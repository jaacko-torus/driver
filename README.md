# Driver

## Dev setup

Make sure to run the following commands before starting development. Bloop is needed regardless of whether using
IntelliJ or metals. This project was built using IntelliJ so there are no guarantees it will work properly in metals.

```sh
# cd driver
mill mill.bsp.BSP/install
mill mill.scalalib.GenIdea/idea
```

### IntelliJ folder tags:

```
driver @ content root
└── src
    ├── main
    │   ├── resources @ resources root
    │   │   └── client @ exclude
    │   │       └── ...
    │   └── scala @ source root
    │       └── ...
    └── test
        └── scala @ test root
            └── ...
```
