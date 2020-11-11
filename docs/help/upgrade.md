# 发布到 NPM

客户端项目的升级和发布顺序

## 项目依赖层级

1. `client`
   1. `@blocklang/bootstrap-classes`
   2. `@blocklang/dojo-fontawesome`
   3. `@blocklang/page-designer`
      1. `@blocklang/bootstrap-classes`
      2. `@blocklang/dojo-fontawesome`
      3. `@blocklang/designer-core`
      4. `@blocklang/std-ide-widget`
         1. `@blocklang/designer-core`
         2. `@blocklang/std-widget-web`

升级顺序为：

1. `@blocklang/bootstrap-classes` - `~0.0.3-alpha.8`
2. `@blocklang/dojo-fontawesome` - `0.0.3-alpha.3`
3. `@blocklang/designer-core` - `0.0.1-alpha.97`
4. `@blocklang/std-widget-web` - `0.0.6-alpha.3`
5. `@blocklang/std-ide-widget` - `0.0.3-alpha.24`
6. `@blocklang/page-designer` - `0.0.3-alpha.21`
7. `client`