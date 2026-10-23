package io.github.fabb.wigai.mcp;

import io.github.fabb.wigai.WigAIExtensionDefinition;
import io.github.fabb.wigai.bitwig.BitwigApiFacade;
import io.github.fabb.wigai.bitwig.BitwigEditFacade;
import io.github.fabb.wigai.bitwig.BitwigContainerFacade;
import io.github.fabb.wigai.bitwig.BitwigEventStream;
import io.github.fabb.wigai.bitwig.BitwigProjectFacade;
import io.github.fabb.wigai.bitwig.BitwigSessionFacade;
import io.github.fabb.wigai.bitwig.DeviceCatalog;
import io.github.fabb.wigai.common.Logger;
import io.github.fabb.wigai.common.logging.StructuredLogger;
import io.github.fabb.wigai.config.ConfigManager;
import io.github.fabb.wigai.features.TransportController;
import io.github.fabb.wigai.features.DeviceController;
import io.github.fabb.wigai.features.ClipSceneController;
import io.modelcontextprotocol.server.*;
import io.modelcontextprotocol.server.transport.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.servlet.ServletHolder;
import io.github.fabb.wigai.mcp.tool.StatusTool;
import io.github.fabb.wigai.mcp.tool.TransportTool;
import io.github.fabb.wigai.mcp.tool.DeviceParamTool;
import io.github.fabb.wigai.mcp.tool.ClipTool;
import io.github.fabb.wigai.mcp.tool.SceneTool;
import io.github.fabb.wigai.mcp.tool.ListTracksTool;
import io.github.fabb.wigai.mcp.tool.ListDevicesOnTrackTool;
import io.github.fabb.wigai.mcp.tool.GetTrackDetailsTool;
import io.github.fabb.wigai.mcp.tool.GetDeviceDetailsTool;
import io.github.fabb.wigai.mcp.tool.ListScenesTool;
import io.github.fabb.wigai.mcp.tool.GetClipsInSceneTool;
import io.modelcontextprotocol.spec.McpSchema;
import com.bitwig.extension.controller.api.ControllerHost;
import io.github.fabb.wigai.mcp.tool.SceneByNameTool;
import io.github.fabb.wigai.mcp.tool.EditTools;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the MCP server for the WigAI extension.
 * Responsible for configuring and managing the MCP HTTP servlet
 * that uses the Server-Sent Events (SSE) transport with the MCP Java SDK.
 *
 * This implementation:
 * - Sets up the MCP Java SDK with SSE transport
 * - Implements standard MCP ping functionality
 * - Registers custom tools like the "status" tool and "transport_start" tool
 * - Configures the appropriate error handling
 * - Provides logging for MCP requests and responses
 * - Registers the MCP servlet with the provided ServletContextHandler
 */
public class McpServerManager {
    /** Sent to clients on initialize; the full guide is docs/AGENT-GUIDE.md in the repository. */
    static final String AGENT_INSTRUCTIONS = """
        WigAI controls the Bitwig Studio project that is open right now.
        Start with status and list_tracks. Read before you write: get_track_mixer, get_device_chains and \
        get_device_parameters do not move the user's selection. If something is silent, check project_state \
        (a stray solo or mute) and set_engine_active.
        To choose devices, use list_installed_devices (Bitwig devices and every scanned plugin) and \
        describe_device (controls, scanned on demand, and Bitwig's own description). Presets: list_presets, \
        or browser_open with mode replace on a device.
        To follow what the user does, call get_events once for a starting point, then pass next_seq back \
        with wait_ms; watch streams changes as notifications.
        The API cannot change routing (track outputs, Audio Receiver sources), place arranger clips, move \
        tracks, add modulators or automation, or add layers: ask the user to do those by hand.
        Edits are undoable with undo; suggest saving before large changes.""";

    private final Logger logger;
    private final WigAIExtensionDefinition extensionDefinition;
    private final ControllerHost controllerHost;

    private HttpServletStreamableServerTransportProvider transportProvider;

    // Reusable controllers - initialized once during first start
    private BitwigApiFacade bitwigApiFacade;
    private TransportController transportController;
    private DeviceController deviceController;
    private ClipSceneController clipSceneController;
    private BitwigEditFacade bitwigEditFacade;
    private BitwigSessionFacade bitwigSessionFacade;
    private DeviceCatalog deviceCatalog;
    private BitwigProjectFacade bitwigProjectFacade;
    private BitwigContainerFacade bitwigContainerFacade;
    private BitwigEventStream bitwigEventStream;

    /**
     * Creates a new McpServerManager instance.
     *
     * @param logger             The logger to use for logging server events
     * @param configManager      The configuration manager (kept for API compatibility)
     * @param extensionDefinition The extension definition to get version information
     */
    public McpServerManager(Logger logger, ConfigManager configManager, WigAIExtensionDefinition extensionDefinition) {
        this(logger, configManager, extensionDefinition, null);
    }

    /**
     * Creates a new McpServerManager instance with a controller host.
     *
     * @param logger             The logger to use for logging server events
     * @param configManager      The configuration manager (kept for API compatibility)
     * @param extensionDefinition The extension definition to get version information
     * @param controllerHost     The Bitwig controller host, or null if not available
     */
    public McpServerManager(Logger logger, ConfigManager configManager, WigAIExtensionDefinition extensionDefinition, ControllerHost controllerHost) {
        this.logger = logger;
        this.extensionDefinition = extensionDefinition;
        this.controllerHost = controllerHost;
    }

    /**
     * Gets the Bitwig controller host.
     *
     * @return The controller host
     * @throws IllegalStateException if the controller host is not available
     */
    public ControllerHost getHost() {
        if (controllerHost == null) {
            throw new IllegalStateException("Controller host is not available");
        }
        return controllerHost;
    }

    /**
     * Creates and returns the MCP servlet.
     * Configures the server with the SSE transport and registers
     * the standard ping functionality and available tools.
     *
     * @param endpointPath The endpoint path for the MCP servlet
     * @return The configured MCP servlet
     * @throws Exception if servlet creation fails
     */
    public ServletHolder createMcpServlet(String endpointPath) throws Exception {
        // 1. Instantiate ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // 2. Instantiate HttpServletStreamableServerTransportProvider
        this.transportProvider = HttpServletStreamableServerTransportProvider
            .builder()
            .objectMapper(objectMapper)
            .mcpEndpoint(endpointPath)
            .build();

        // 3. Configure the MCP server with tools
        // Note: Direct request/response logging with onRequest/onResponse methods is not
        // supported by the MCP Java SDK. The StatusTool implementation handles its own
        // logging. If more detailed logging is needed, we should investigate alternative
        // approaches with the MCP SDK.

        // Initialize controllers only once during first start to avoid Bitwig API restrictions
        if (bitwigApiFacade == null) {
            logger.info("McpServerManager: Initializing Bitwig API controllers");
            bitwigApiFacade = new BitwigApiFacade(getHost(), logger);
            transportController = new TransportController(bitwigApiFacade, logger);
            deviceController = new DeviceController(bitwigApiFacade, logger);
            clipSceneController = new ClipSceneController(bitwigApiFacade, logger);
            bitwigEditFacade = new BitwigEditFacade(getHost(), logger, bitwigApiFacade.getApplication(),
                bitwigApiFacade.getTransport(), bitwigApiFacade.getTrackBank(), bitwigApiFacade.getTrackDeviceBanks());
            bitwigSessionFacade = new BitwigSessionFacade(getHost(), logger, bitwigApiFacade.getTransport(),
                bitwigApiFacade.getTrackBank());
            bitwigProjectFacade = new BitwigProjectFacade(getHost(), logger, bitwigApiFacade.getTransport(),
                bitwigApiFacade.getTrackBank(), bitwigEditFacade);
            bitwigContainerFacade = new BitwigContainerFacade(getHost(), logger, bitwigEditFacade,
                BitwigContainerFacade.defaultPresetRoots());
            bitwigEventStream = new BitwigEventStream(getHost(), logger, bitwigApiFacade.getApplication(),
                bitwigApiFacade.getTransport(), bitwigApiFacade.getTrackBank());
            deviceCatalog = new DeviceCatalog(DeviceCatalog.defaultFile(), bitwigEditFacade, logger,
                getHost().getHostVersion());
        } else {
            logger.info("McpServerManager: Reusing existing Bitwig API controllers");
        }

        // Create StructuredLogger for tools that have been migrated to unified error handling
        StructuredLogger structuredLogger = new StructuredLogger(logger, "MCP-Tools");

        List<McpServerFeatures.SyncToolSpecification> tools = new ArrayList<>(List.of(
            StatusTool.specification(this.extensionDefinition, bitwigApiFacade, structuredLogger),
            TransportTool.transportStartSpecification(transportController, structuredLogger),
            TransportTool.transportStopSpecification(transportController, structuredLogger),
            ClipTool.launchClipSpecification(clipSceneController, structuredLogger),
            SceneTool.launchSceneByIndexSpecification(clipSceneController, structuredLogger),
            SceneByNameTool.launchSceneByNameSpecification(clipSceneController, structuredLogger),
            DeviceParamTool.getSelectedDeviceParametersSpecification(deviceController, structuredLogger),
            DeviceParamTool.setSelectedDeviceParameterSpecification(deviceController, structuredLogger),
            DeviceParamTool.setMultipleDeviceParametersSpecification(deviceController, structuredLogger),
            GetDeviceDetailsTool.getDeviceDetailsSpecification(deviceController, structuredLogger),
            ListTracksTool.specification(bitwigApiFacade, structuredLogger),
            ListDevicesOnTrackTool.specification(bitwigApiFacade, structuredLogger),
            GetTrackDetailsTool.specification(bitwigApiFacade, structuredLogger),
            ListScenesTool.specification(bitwigApiFacade, structuredLogger),
            GetClipsInSceneTool.getClipsInSceneSpecification(clipSceneController, structuredLogger)
        ));
        tools.addAll(EditTools.specifications(bitwigEditFacade, structuredLogger));
        tools.addAll(EditTools.sessionSpecifications(bitwigEditFacade, bitwigSessionFacade, structuredLogger));
        tools.addAll(EditTools.catalogSpecifications(bitwigEditFacade, deviceCatalog, structuredLogger));
        tools.addAll(EditTools.eventSpecifications(bitwigEditFacade, bitwigEventStream, structuredLogger));
        tools.addAll(EditTools.containerSpecifications(bitwigEditFacade, bitwigContainerFacade, structuredLogger));
        tools.addAll(EditTools.projectSpecifications(bitwigEditFacade, bitwigSessionFacade, bitwigProjectFacade, structuredLogger));

        McpServer.sync(this.transportProvider)
            .serverInfo("WigAI", extensionDefinition.getVersion())
            .instructions(AGENT_INSTRUCTIONS)
            .capabilities(McpSchema.ServerCapabilities.builder()
                .tools(true)
                .logging()
                .build())
            .tools(tools)
            .build();

        // 4. Return the MCP servlet
        return new ServletHolder(this.transportProvider);
    }
}
