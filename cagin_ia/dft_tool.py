import math
from typing import List, Tuple

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt


PIECEWISE_LIMIT = 4.10887


def g(x: float) -> float:
    """Piecewise definition of g(x) on [0, 4.10887]."""
    if 0 <= x <= 0.48:
        return 0.979167 * x
    if 0.48 < x <= 0.54:
        return 0.47
    if 0.54 < x <= 0.6:
        return -1 * x + 1.01
    if 0.6 < x <= 0.76:
        return 0.41
    if 0.76 < x <= 0.85:
        return 0.555556 * x - 0.0122222
    if 0.85 < x <= 1.07:
        return -0.0454545 * x + 0.498636
    if 1.07 < x <= 1.163:
        return -1.23656 * x + 1.77312
    if 1.163 < x <= 1.3:
        return 0.335
    if 1.3 < x <= 1.47:
        return 0.564706 * x - 0.399118
    if 1.47 < x <= 1.738:
        return -1.10075 * x + 2.0491
    if 1.738 < x <= 1.923:
        return 0.136
    if 1.923 < x <= 1.956:
        return 4.48485 * x - 8.48836
    if 1.956 < x <= 2.195:
        return 0.635983 * x - 0.959983
    if 2.195 < x <= 2.249:
        return 0.436
    if 2.249 < x <= 2.3:
        return -1.2549 * x + 3.25827
    if 2.3 < x <= 2.481:
        return 0.372
    if 2.481 < x <= 2.552:
        return 0.816901 * x - 1.65473
    if 2.552 < x <= 2.751:
        return 0.43
    if 2.751 < x <= 2.898:
        return -1.12245 * x + 3.51786
    if 2.898 < x <= 3.042:
        return 0.265
    if 3.042 < x <= 3.177:
        return 1.17778 * x - 3.3178
    if 3.177 < x <= 3.436:
        return 0.424
    if 3.436 < x <= PIECEWISE_LIMIT:
        return -0.630137 * x + 2.58915
    raise ValueError(f"x={x} outside supported interval [0, {PIECEWISE_LIMIT}]")


def generate_samples(num_points: int, x_limit: float) -> Tuple[List[float], List[float], float]:
    if num_points < 2:
        raise ValueError("Need at least two sample points")

    delta_x = x_limit / (num_points - 1)
    xs = [i * delta_x for i in range(num_points)]
    ys = []

    for i, x in enumerate(xs):
        if i == 0 or i == num_points - 1:
            ys.append(0.0)
        else:
            ys.append(g(x))

    return xs, ys, delta_x


def discrete_fourier_transform(signal: List[float], delta_x: float) -> List[complex]:
    n_points = len(signal)
    spectrum: List[complex] = []

    for n in range(n_points):
        re = 0.0
        im = 0.0
        for k in range(n_points):
            theta = 2 * math.pi * n * k / (n_points * delta_x)
            re += signal[k] * math.cos(theta)
            im -= signal[k] * math.sin(theta)
        spectrum.append(complex(re, im))
    return spectrum


def inverse_discrete_fourier_transform(spectrum: List[complex], delta_x: float) -> List[complex]:
    n_points = len(spectrum)
    time_signal: List[complex] = []

    for n in range(n_points):
        re = 0.0
        im = 0.0
        for k in range(n_points):
            theta = 2 * math.pi * n * k / (n_points * delta_x)
            re += spectrum[k].real * math.cos(theta) - spectrum[k].imag * math.sin(theta)
            im += spectrum[k].real * math.sin(theta) + spectrum[k].imag * math.cos(theta)
        time_signal.append(complex(re / n_points, im / n_points))
    return time_signal


def print_samples(xs: List[float], ys: List[float]) -> None:
    print("Sample points (index, x, y):")
    for i, (x, y) in enumerate(zip(xs, ys)):
        print(f"  {i:3d}: x={x:.6f}, y={y:.6f}")
    print()


def print_spectrum(spectrum: List[complex], precision: int = 4) -> None:
    for i, value in enumerate(spectrum):
        re = round(value.real, precision)
        im = round(value.imag, precision)
        sign = '-' if im < 0 else '+'
        formatted_im = abs(im)
        print(f"X({i}) = {re} {sign} ({formatted_im})i")
    print()


def plot_results(xs: List[float], ys: List[float], reconstructed: List[complex], x_limit: float) -> None:
    dense_x = [i * (x_limit / 500) for i in range(501)]
    dense_y = [g(x) for x in dense_x]

    plt.figure(figsize=(10, 6))
    plt.plot(dense_x, dense_y, label="g(x) piecewise", linewidth=2)
    plt.plot(xs, ys, "o", label="Sampled g(x)")
    plt.plot(xs, [z.real for z in reconstructed], label="Reconstructed IDFT", linestyle="--")
    plt.xlabel("x")
    plt.ylabel("Amplitude")
    plt.title("Piecewise g(x) and IDFT reconstruction")
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig("dft_reconstruction.png", dpi=200)
    print("Saved plot to dft_reconstruction.png")


def main() -> None:
    default_points = 40
    default_limit = PIECEWISE_LIMIT

    try:
        num_points_input = input(f"Enter number of sample points [default {default_points}]: ").strip()
        num_points = int(num_points_input) if num_points_input else default_points

        x_limit_input = input(f"Enter domain upper limit x_max [default {default_limit}]: ").strip()
        x_limit = float(x_limit_input) if x_limit_input else default_limit
    except ValueError as exc:
        raise SystemExit(f"Invalid input: {exc}")

    xs, ys, delta_x = generate_samples(num_points, x_limit)

    print(f"Using delta x = {delta_x:.6f} (computed as x_max/(N-1) with N={num_points})")
    print_samples(xs, ys)

    spectrum = discrete_fourier_transform(ys, delta_x)
    print("DFT values:")
    print_spectrum(spectrum)

    reconstructed = inverse_discrete_fourier_transform(spectrum, delta_x)
    print("IDFT values (real and imaginary components):")
    print_spectrum(reconstructed)

    plot_results(xs, ys, reconstructed, x_limit)


if __name__ == "__main__":
    main()
