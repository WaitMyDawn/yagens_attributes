# Project development

## Logs and temporary outputs

- Store captured build, data-generation and validation console logs in the root project's `build/logs/` directory. Create that directory before redirecting output. Do not create `.log` files in the repository root or a platform module root.
- Store disposable runtime instances and reports under the selected platform's `build/` directory. Keep normal development instances under that platform's `run/` directory.
- Headless adapter tests use console-only logging. Their console output is captured by the invoking build; do not enable file appenders that resolve paths against the platform module root.
- Keep logs, previews and temporary reports out of source directories and version control. Retain the latest relevant validation evidence; remove superseded console captures when cleaning up.
- Document verification outcomes separately from log retention. If historical logs are removed, identify the results as historical records instead of linking to nonexistent evidence.

See `DEVELOPMENT.md` for the logging command convention and `Spec.md` for verification status.

## In-game development editors

- Root exported drafts and other editor session outputs under `.editor/<tool>/` in the active game instance directory. Resolve that directory from the running Minecraft instance, not the repository, shell working directory or a guessed `run/client` path.
- Keep development UI and its export/inspection classes in a removable source set. Release artifacts must not depend on editor classes or assets. A removed editor directory must not break ordinary builds.
- Export structured drafts with stable rule IDs, the original rule and a baseline fingerprint. Reconcile against current authoring defaults before updating generators; never silently replace unrelated source changes.
- Keep confirmed rules, required runtime functions and runData outputs when removing an editor. Session drafts belong to the instance and are cleaned only when their removal is included in the requested scope.
- For graphical loot-source discovery, native table editing, batch authoring and generator reconciliation, use the `yagen-loot-workbench` skill. Its canonical local source is `../Yagen-Loot-Workbench/skills/yagen-loot-workbench/SKILL.md`; the host editor is detached after authoring. Keep editor implementation and reusable instructions outside production sources.

## Texture authoring

- Use the independent Yagen-Pixel checkout and its `skills/yagen-pixel/SKILL.md` for the local pixel workflow. In the standard sibling checkout layout, the tool is at `../Yagen-Pixel`.
- Keep editable batches, reference packs and review records under `art/textures/`; keep candidates and previews under `build/texture-preview/<batch>/`. Publish accepted PNGs to `common/src/main/resources/` using their declared texture resource paths. Follow `art/textures/README.md` for path ownership.
- Read actual Mod definitions and their explicit texture fields to establish artwork scope and output paths. Do not infer effect semantics from names or ID suffixes.
- Treat active reference catalogs and selection records as the authority for eligible samples. Retired records are historical metadata and must not be used as references.
- Classify art inputs as approved references, explicit edit targets or comparison history. Rejected drafts must not become positive style/shape references unless explicitly selected as edit targets. Keep comparison history out of new drawing dependencies and label it separately in previews; an image being available does not establish approval.
- Approved subject PNGs are static resources. `ModDataProvider` consults the existing client-resource manager and generates placeholders only for missing textures. Keep each resource path in exactly one of the static/generated source roots; do not edit generated placeholders as an artwork source.
- For newly supplied texture PNGs, preserve the original in its art batch and publish through `scripts/optimize_png.py` using its default 5% opacity cleanup, non-rendering text/density metadata removal and lossless encoding. Strictly below 5% means alpha 1–12 becomes transparent; retain all other RGBA values. Reports and provenance must distinguish this approved pixel cleanup from lossless encoding. Use `--alpha-cutoff-percent 0` only when explicit asset instructions require exact source RGBA preservation. Do not sweep unrelated accepted textures without authorization.
- Keep texture-only integration proportional: inspect the changed PNG once for dimensions, transparency and its declared destination, then publish it. Do not run combat tests, full builds, repeated hash comparisons or `runData` for an existing texture replacement. Broaden verification only when generator, model, resource binding or runtime code changes, or when a concrete failure needs investigation.
