This is a Kotlin Multiplatform project targeting Desktop (JVM).

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## Python DFT helper

The repository also includes a small Python utility (`dft_tool.py`) that mirrors the discrete Fourier transform
and inverse transform behavior demonstrated in the linked mobile example.

Run it with:

```bash
pip install -r requirements.txt
python dft_tool.py
```

The script prompts for the number of sample points and the domain upper bound, computes the spacing
Δx = x_max/(N−1), pins the first and last samples to zero, performs the DFT/IDFT using the
specified Δx scaling, and saves an overlay plot (`dft_reconstruction.png`) comparing the piecewise g(x)
against the reconstructed signal.