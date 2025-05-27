package io.github.fabb.wigai.bitwig;

import com.bitwig.extension.controller.api.SceneBank;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.Scene;
import io.github.fabb.wigai.common.Logger;

/**
 * Facade for Bitwig Scene Bank operations (scene name lookup, etc).
 */
public class SceneBankFacade {
    private final SceneBank sceneBank;
    private final Logger logger;
    private final int sceneCount;

    public SceneBankFacade(ControllerHost host, Logger logger, int sceneCount) {
        this.logger = logger;
        this.sceneCount = sceneCount;
        this.sceneBank = host.createSceneBank(sceneCount);
        for (int i = 0; i < sceneCount; i++) {
            Scene scene = sceneBank.getItemAt(i);
            scene.name().markInterested();
            scene.exists().markInterested();
        }
    }

    public int getSceneCount() {
        return sceneCount;
    }

    public String getSceneName(int index) {
        if (index < 0 || index >= sceneCount) return null;
        Scene scene = sceneBank.getItemAt(index);
        if (scene.exists().get()) {
            return scene.name().get();
        }
        return null;
    }

    /**
     * Finds the first scene index with the given name (case-sensitive).
     * Returns -1 if not found.
     */
    public int findSceneByName(String sceneName) {
        for (int i = 0; i < sceneCount; i++) {
            Scene scene = sceneBank.getItemAt(i);
            if (scene.exists().get() && sceneName.equals(scene.name().get())) {
                return i;
            }
        }
        return -1;
    }
}
