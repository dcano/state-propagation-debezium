---
description: Implements the boiler plate of a new command
argument-hint: Short command description
allowed-tools: Read, Write, Glob
---

You are helping to spin up a new command for this application, from a short idea provided in the user input below.

User input: $ARGUMENTS

## High level behaviour

Provide the command scaffolding, you need to create:

- **The command definition class:** As class name ("command_class_name") use `<action><aggregate>Command`, e.g.  `UpdateCourseDurationCommand`. Add lombok annotation for package private getters.
- **The command handler class:**  `<command_class_name>Handler`. The handler extends implements `CommandHandler<command_class_name>`
- **The command handler test class**: `<command_class_name>HandlerTest`.

Command, handler and handler tests must be all of them in the same package.

Try to Infer from the provided $ARGUMENTS and the current project context and memory:

- The <action>
- The <aggregate>
- The maven module (always a `-application` module)
- The package name.

If you cannot infer any of those, ask the user. Do not create code inside the handler method. Do not create properties in the command definition. Just provide the scaffolding.