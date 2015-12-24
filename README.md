# asm.thi.ng

Static site generator for http://asm.thi.ng

## Usage

All site content defined in `resources/content.edn`

```
lein repl

(require 'core)
(in-ns 'core)
(spit "resources/public/index.html" (generate-page config))
```

Use `upload.sh` to deploy to S3
