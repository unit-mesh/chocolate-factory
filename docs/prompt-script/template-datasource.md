---
layout: default
title: TemplateDatasource
parent: Prompt Script
nav_order: 11
permalink: /prompt-script/template-datasource
---

{: .warning }
Automatically generated documentation; use the command `./gradlew :docs-builder:run` and update comments in the source code to reflect changes.

# TemplateDatasource 

> TemplateDatasource is the job's template datasource config, which will be used for render template.
The datasource can be a file, a directory or an http url, or a string, which be auto loaded by extension.
For example:

```yaml
template-datasource:
   - type: file
     value: datasource.json
```

We will load the datasource.json file as the template datasource.

## File 

File is a file datasource, which will load data from a file.

