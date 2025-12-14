# WigAI Project Documentation Index

**WigAI** is a Bitwig Studio extension that enables AI agents to control music production through the Model Context Protocol (MCP).

**Quick Links:**
- 📊 [Project Overview](project-overview.md) — Start here for executive summary
- 🎯 [Architecture Overview](architecture.md) — System design
- 📚 [API Reference](api-reference.md) — Full MCP API spec
- 🏗️ [Component Deep Dive](component-architecture-deep-dive.md) — Detailed component breakdown

---

## 📚 Documentation By Category

### 🎯 **Getting Started**
- [**Project Overview**](project-overview.md) - Executive summary, architecture at a glance, tech stack
- [**Project Brief**](project-brief.md) - High-level project goals and scope
- [**Architecture Overview**](architecture.md) - Main system architecture and design patterns

### 🏗️ **Architecture & Design**
- [**Component Architecture - Deep Dive**](component-architecture-deep-dive.md) - Detailed breakdown of every major component
- [**Component View**](component-view.md) - Component structure and organization
- [**Sequence Diagrams**](sequence-diagrams.md) - Key workflows and interactions
- [**Data Models**](data-models.md) - Core data structures and entity models

### 🔌 **API & Integration**
- [**API Reference**](api-reference.md) - Complete MCP API specification with all endpoints
- [**MCP Tools Reference**](mcp-tools-reference.md) - Detailed tool implementations and error codes
- [**Key References**](key-references.md) - External documentation links and resources

### 🛠️ **Development**
- [**Tech Stack**](tech-stack.md) - Technology choices and justification
- [**Project Structure**](project-structure.md) - Directory layout and module organization
- [**Operational Guidelines**](operational-guidelines.md) - Coding standards, testing, error handling
- [**Environment Variables**](environment-vars.md) - Configuration and environment setup
- [**Semantic Versioning Guide**](semantic-versioning-guide.md) - Version management strategy

### 🚀 **Deployment & Operations**
- [**Infrastructure & Deployment**](infra-deployment.md) - Deployment procedures and infrastructure
- [**Testing Architecture**](testing/mcp-endpoints-verification.md) - MCP endpoint verification tests

### 📋 **Requirements & Planning**
- [**Product Requirements**](prd/) - Full PRD organized by epic
- [**Implementation Stories**](stories/) - Feature stories and implementation details

### 📊 **Workflow & Status**
- [**Workflow Status**](bmm-workflow-status.yaml) - BMM methodology progress tracking

---

## 🎯 **Quick Navigation by Use Case**

### I need to...

**...understand what this project does**
→ Start with [Project Overview](project-overview.md), then read [Project Brief](project-brief.md)

**...understand the architecture**
→ Read [Architecture Overview](architecture.md), then dive into [Component Architecture Deep Dive](component-architecture-deep-dive.md)

**...integrate with the MCP API**
→ Review [API Reference](api-reference.md), then check [MCP Tools Reference](mcp-tools-reference.md) for tool details

**...add a new feature**
→ Find your story in [stories/](stories/), review [Architecture](architecture.md), check [Component Deep Dive](component-architecture-deep-dive.md) for patterns

**...understand data flow**
→ Check [Sequence Diagrams](sequence-diagrams.md) and [Data Models](data-models.md)

**...set up development**
→ Read [Project Structure](project-structure.md), [Environment Variables](environment-vars.md), [Tech Stack](tech-stack.md)

**...understand coding standards**
→ Review [Operational Guidelines](operational-guidelines.md)

**...deploy the extension**
→ Check [Infrastructure & Deployment](infra-deployment.md)

---

## 📊 **Project Metadata**

| Attribute | Value |
|-----------|-------|
| **Project Type** | Bitwig Studio Extension |
| **Language** | Java 21 (LTS) |
| **Build System** | Gradle (Kotlin DSL) |
| **Repository Type** | Monolith |
| **API Protocol** | MCP 0.11.0+ |
| **HTTP Server** | Jetty 11 (SSE/Streamable HTTP) |
| **Test Framework** | JUnit Jupiter 5 |
| **Target Platform** | Bitwig Studio 12+ |
| **Status** | Active Development |

---

## 🔍 **Document Organization**

### Architecture Docs (For Understanding System Design)
- For **system overview**: [Architecture Overview](architecture.md)
- For **component details**: [Component Architecture - Deep Dive](component-architecture-deep-dive.md)
- For **interactions**: [Sequence Diagrams](sequence-diagrams.md)
- For **data structures**: [Data Models](data-models.md)

### API Docs (For Integration)
- For **endpoint specifications**: [API Reference](api-reference.md)
- For **tool implementations**: [MCP Tools Reference](mcp-tools-reference.md)
- For **error handling**: [MCP Tools Reference - Error Codes](mcp-tools-reference.md#error-codes)

### Dev Docs (For Development)
- For **setup**: [Project Structure](project-structure.md), [Environment Variables](environment-vars.md)
- For **standards**: [Operational Guidelines](operational-guidelines.md)
- For **tech choices**: [Tech Stack](tech-stack.md)
- For **features**: [Implementation Stories](stories/)

### Requirements Docs (For Understanding Scope)
- For **product goals**: [Project Brief](project-brief.md)
- For **detailed requirements**: [Product Requirements Directory](prd/)
- For **implementation tasks**: [Stories Directory](stories/)

---

## 📈 **Key Files by Category**

| Purpose | Files |
|---------|-------|
| **Architecture** | `architecture.md`, `component-architecture-deep-dive.md`, `component-view.md` |
| **API** | `api-reference.md`, `mcp-tools-reference.md` |
| **Data** | `data-models.md` |
| **Dev Setup** | `project-structure.md`, `environment-vars.md`, `tech-stack.md` |
| **Operations** | `operational-guidelines.md`, `infra-deployment.md`, `semantic-versioning-guide.md` |
| **Planning** | `prd/`, `stories/`, `project-brief.md` |

---

## ✅ **For AI Agents**

When using these docs as context:

1. **Start with:** [Project Overview](project-overview.md) for rapid context loading
2. **For architecture questions:** Check [Component Deep Dive](component-architecture-deep-dive.md)
3. **For API questions:** Reference [API Reference](api-reference.md) + [MCP Tools Reference](mcp-tools-reference.md)
4. **For data questions:** Use [Data Models](data-models.md)
5. **For feature development:** Check related story in [stories/](stories/)

---

**Last Updated:** 2025-12-13  
**Generated by:** BMM Document Project Workflow  
**Status:** Active Development (Phase 3 - Implementation)
