# Project Brief: WigAI

## Introduction / Problem Statement

WigAI aims to enhance the creative workflow for Bitwig musicians by developing a server that leverages the **Model Context Protocol (MCP)** to enable AI-driven control over the software. Currently, musicians seeking novel ways to interact with and modulate synthesizer parameters and notes within Bitwig may find existing control methods direct but lacking in intelligent automation or voice-activated convenience that can understand the deeper musical context. This project allows users to interact with Bitwig via an AI agent, either through voice commands for specific changes or by instructing the agent to introduce creatively guided (semi-random) modifications that are informed by the model context. The increasing traction and adoption of the Model Context Protocol make this an opportune moment to introduce an intelligent control layer, opening up new avenues for context-aware sonic exploration and hands-free operation.

## Vision & Goals

* **Vision:** WigAI will become the go-to intelligent assistant for Bitwig users, seamlessly integrating AI into their creative process and unlocking new levels of musical expression.
* **Primary Goals (MVP):**
    1.  Enable AI-driven control (e.g., via voice commands or instructed AI actions) to successfully start and stop playback in Bitwig.
    2.  Enable AI-driven control to successfully trigger a user-specified clip in Bitwig.
    3.  Enable AI-driven control to accurately read and set the eight (8) parameter values of the currently selected/cursor device in Bitwig.
* **Success Metrics (Initial Ideas):**
    * Number of unique Bitwig parameters successfully controlled via WigAI.

## Target Audience / Users

The primary users for WigAI are **live performing musicians**, encompassing both **hobbyists and professionals**. Their key needs that WigAI aims to address include:
* **Creative Inspiration:** Leveraging AI to generate unexpected or novel musical variations and ideas.
* **Hands-Free Control:** The ability to make both precise parameter adjustments and trigger broader creative changes without manual intervention, crucial in a live performance setting. This includes both exact, instructed changes and more serendipitous, "inspiring surprises" initiated by the AI.

For the initial MVP, the focus is on this core group without further differentiation into sub-types.

## Key Features / Scope (High-Level Ideas for MVP)

The Minimum Viable Product (MVP) for WigAI will focus on delivering the following core features, enabling AI-driven interaction with Bitwig:

* **AI-driven Transport Control:** The AI agent will be able to start and stop Bitwig's playback based on user commands (e.g., voice input or other AI triggers).
* **AI-driven Clip Launching:** The AI agent will be able to trigger specific clips within a Bitwig project based on user commands.
* **AI-driven Device Parameter Control:** The AI agent will interface with Bitwig to read and modify the eight (8) parameters of the currently selected device. This includes responding to precise user instructions for parameter value changes as well as more abstract creative prompts (e.g., "make the sound brighter," "add some rhythmic movement").

## Known Technical Constraints or Preferences

* **Constraints:**
    * **Budget:** $0. The project must exclusively use open-source tools and technologies.
    * **Timeline:** None specified, as this is a hobby project. Development will proceed as time allows.
    * **Mandatory Technology:** The project *must* use the official Bitwig Java extension API (specifically referencing version 22: `https://maven.bitwig.com/com/bitwig/extension-api/22/`).
    * **Operating System Compatibility:** The solution must function on all operating systems supported by Bitwig Studio (macOS, Windows, and Linux).
    * **Programming Language:** Java (implied by the mandatory Bitwig extension API).
* **Risks:**
    * **Development Time Scarcity:** As a hobby project, available development time is limited. (Mitigation: AI code assistants will be utilized to aid development).
    * **API Evolution:** Bitwig releases API updates every few months. While the API is reported to be relatively stable, changes could necessitate updates to WigAI to maintain compatibility.
    * **Community Adoption/Engagement:** Successful adoption may depend on engagement and visibility within Bitwig community forums (e.g., official forums, Discord, Reddit). This is where initial user feedback and awareness will be cultivated.

## Relevant Research (Optional)

Initial research and exploration have included:

* **Model Context Protocol (MCP):** Familiarization with the MCP specifications via `https://modelcontextprotocol.io/`, including the development of a small local demo application in TypeScript to understand its practical implementation.
* **Bitwig Extension API Examples:** Awareness of existing complex Bitwig extensions that utilize the Java API, such as "DrivenByMoss" (`https://github.com/git-moss/DrivenByMoss`), which serves as a reference for API capabilities and extension structure.
