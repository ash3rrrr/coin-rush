"""Generate Coin Rush sound effects as WAV files using pure synthesis.

Produces small (few-KB), license-free 44.1 kHz mono 16-bit WAVs in
app/src/main/res/raw/. Run from the repo root:

    python scripts/gen_sounds.py
"""

import math
import os
import struct
import wave

SR = 44100
OUT_DIR = os.path.join("app", "src", "main", "res", "raw")


def write_wav(name, samples):
    os.makedirs(OUT_DIR, exist_ok=True)
    path = os.path.join(OUT_DIR, name)
    with wave.open(path, "wb") as f:
        f.setnchannels(1)
        f.setsampwidth(2)
        f.setframerate(SR)
        frames = b"".join(
            struct.pack("<h", max(-32767, min(32767, int(s * 32767))))
            for s in samples
        )
        f.writeframes(frames)
    print(f"wrote {path} ({len(samples) / SR:.2f}s)")


def env(t, dur, attack=0.005):
    """Simple attack/decay envelope."""
    if t < attack:
        return t / attack
    return max(0.0, 1.0 - (t - attack) / (dur - attack))


def coin_sound():
    """Bright two-tone coin pickup: B5 -> E6."""
    dur = 0.18
    samples = []
    for i in range(int(SR * dur)):
        t = i / SR
        freq = 987.77 if t < 0.08 else 1318.51
        s = 0.6 * math.sin(2 * math.pi * freq * t) * env(t, dur)
        samples.append(s)
    return samples


def crash_sound():
    """Descending buzz for the crash."""
    dur = 0.45
    samples = []
    for i in range(int(SR * dur)):
        t = i / SR
        freq = 220 * (1 - 0.8 * t / dur)
        s = (
            0.7 * math.sin(2 * math.pi * freq * t)
            + 0.2 * math.sin(2 * math.pi * freq * 2.7 * t)
        ) * env(t, dur, attack=0.002)
        samples.append(s * 0.9)
    return samples


def swish_sound():
    """Quick filtered-noise-style swish for lane switch (synthesized, not noise)."""
    dur = 0.09
    samples = []
    for i in range(int(SR * dur)):
        t = i / SR
        freq = 500 + 2500 * (t / dur)
        s = 0.35 * math.sin(2 * math.pi * freq * t) * env(t, dur, attack=0.002)
        samples.append(s)
    return samples


if __name__ == "__main__":
    write_wav("coin.wav", coin_sound())
    write_wav("crash.wav", crash_sound())
    write_wav("swish.wav", swish_sound())
