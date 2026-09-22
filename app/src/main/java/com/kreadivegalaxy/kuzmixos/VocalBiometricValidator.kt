package com.kreadivegalaxy.kuzmixos

import android.content.Context
import android.util.Log
import kotlin.math.abs
import kotlin.math.exp

class VocalBiometricValidator(private val context: Context) {
    
    private val biometricsManager = VocalBiometricsManager(context)
    private val spectralSubtractor = SpectralSubtractor(256)

    /**
     * Gets the registered owner's Acoustic Signature Vector.
     * Enrolled during onboarding or falls back to a preset signature vector if none exists.
     */
    fun getEnrolledSignature(): List<Float> {
        val signatureStr = biometricsManager.vocalSignature
        if (signatureStr.isNullOrEmpty()) {
            // Default enrolled signature vector if not explicitly calibrated yet
            return listOf(0.55f, 0.65f, 0.75f, 0.85f, 0.95f)
        }
        return try {
            signatureStr.split(",").map { it.trim().toFloat() }
        } catch (e: Exception) {
            listOf(0.55f, 0.65f, 0.75f, 0.85f, 0.95f)
        }
    }

    /**
     * Extracts spectral envelope / acoustic energy features from the raw PCM buffer.
     * Partitions the buffer into chunks corresponding to the signature dimensions.
     */
    fun extractLiveFeatures(buffer: ShortArray): FloatArray {
        val size = 5
        val features = FloatArray(size)
        if (buffer.isEmpty()) return FloatArray(size) { 0.5f }
        
        val frameSize = buffer.size / size
        if (frameSize <= 0) return FloatArray(size) { 0.5f }

        for (i in 0 until size) {
            val start = i * frameSize
            val end = minOf(start + frameSize, buffer.size)
            var sumSquare = 0.0
            var count = 0
            for (j in start until end) {
                val sample = buffer[j].toDouble() / 32768.0
                sumSquare += sample * sample
                count++
            }
            val rms = if (count > 0) Math.sqrt(sumSquare / count) else 0.0
            features[i] = rms.toFloat()
        }

        // Apply scaling and normalize relative to peak to ensure speaker independence is mitigated and matched
        val maxVal = features.maxOrNull() ?: 1f
        if (maxVal > 0f) {
            for (i in features.indices) {
                features[i] = (features[i] / maxVal) * 0.9f + 0.1f
            }
        } else {
            for (i in features.indices) {
                features[i] = 0.5f
            }
        }
        return features
    }

    /**
     * Computes the Dynamic Time Warping (DTW) distance between two multi-dimensional feature sequences.
     */
    fun dtwDistance(seq1: FloatArray, seq2: FloatArray): Float {
        val n = seq1.size
        val m = seq2.size
        val dtw = Array(n + 1) { FloatArray(m + 1) { Float.MAX_VALUE } }
        dtw[0][0] = 0f

        for (i in 1..n) {
            for (j in 1..m) {
                val cost = abs(seq1[i - 1] - seq2[j - 1])
                val minPrev = minOf(dtw[i - 1][j], dtw[i][j - 1], dtw[i - 1][j - 1])
                if (minPrev != Float.MAX_VALUE) {
                    dtw[i][j] = cost + minPrev
                }
            }
        }
        return dtw[n][m]
    }

    /**
     * Validates live PCM audio against the enrolled vocal signature.
     * Returns a match confidence similarity score between 0.0f and 1.0f.
     */
    fun validateVoice(buffer: ShortArray): Float {
        // Denoise PCM buffer using Spectral Subtraction prior to feature comparison
        val denoised = spectralSubtractor.denoise(buffer)
        
        val enrolled = getEnrolledSignature().toFloatArray()
        val live = extractLiveFeatures(denoised)
        
        val distance = dtwDistance(enrolled, live)
        
        // Map distance using a precise exponential decaying transfer function
        // so that a close match yields >= 0.98f and minor variances drop below 98%
        val similarity = exp(-0.05 * distance).toFloat()
        
        Log.d("VocalBiometricValidator", "DTW Distance: $distance, Computed Similarity: $similarity")
        return similarity.coerceIn(0.0f, 1.0f)
    }

    /**
     * On-device implementation of a Spectral Subtraction algorithm
     * operating on Short-Time Discrete Fourier Transform (STFT) frames.
     */
    private class SpectralSubtractor(private val fftSize: Int = 256) {
        private val halfSize = fftSize / 2
        private val cosTable = FloatArray(fftSize * (halfSize + 1))
        private val sinTable = FloatArray(fftSize * (halfSize + 1))
        private val window = FloatArray(fftSize)

        init {
            // Precompute trigonometric tables for high-performance execution
            for (k in 0..halfSize) {
                for (n in 0 until fftSize) {
                    val angle = (2.0 * Math.PI * n * k / fftSize).toFloat()
                    cosTable[k * fftSize + n] = kotlin.math.cos(angle)
                    sinTable[k * fftSize + n] = kotlin.math.sin(angle)
                }
            }
            // Generate standard Hanning window
            for (n in 0 until fftSize) {
                window[n] = (0.5 * (1.0 - kotlin.math.cos(2.0 * Math.PI * n / (fftSize - 1)))).toFloat()
            }
        }

        fun denoise(input: ShortArray): ShortArray {
            if (input.size < fftSize) return input.clone()

            val hopSize = fftSize / 2
            val numSamples = input.size
            val output = FloatArray(numSamples)
            val windowSum = FloatArray(numSamples)

            // Step 1: Estimate Noise Spectrum from first few frames (assumed silence/background noise)
            val noiseSpectrum = FloatArray(halfSize + 1) { 0f }
            var noiseFrameCount = 0
            var frameIdx = 0
            
            while (frameIdx + fftSize <= numSamples && noiseFrameCount < 3) {
                val frame = FloatArray(fftSize)
                for (n in 0 until fftSize) {
                    frame[n] = (input[frameIdx + n].toFloat() / 32768.0f) * window[n]
                }
                
                // Compute magnitude spectrum
                for (k in 0..halfSize) {
                    var re = 0f
                    var im = 0f
                    for (n in 0 until fftSize) {
                        val c = cosTable[k * fftSize + n]
                        val s = sinTable[k * fftSize + n]
                        re += frame[n] * c
                        im -= frame[n] * s
                    }
                    val mag = kotlin.math.sqrt(re * re + im * im)
                    noiseSpectrum[k] += mag
                }
                noiseFrameCount++
                frameIdx += hopSize
            }

            if (noiseFrameCount > 0) {
                for (k in 0..halfSize) {
                    noiseSpectrum[k] /= noiseFrameCount
                }
            }

            // Step 2: Overlap-Add Short-Time Fourier Transform denoising loop
            var offset = 0
            while (offset + fftSize <= numSamples) {
                val frame = FloatArray(fftSize)
                for (n in 0 until fftSize) {
                    frame[n] = (input[offset + n].toFloat() / 32768.0f) * window[n]
                }

                // Forward DFT
                val re = FloatArray(halfSize + 1)
                val im = FloatArray(halfSize + 1)
                val mag = FloatArray(halfSize + 1)
                val phase = FloatArray(halfSize + 1)

                for (k in 0..halfSize) {
                    var r = 0f
                    var i = 0f
                    for (n in 0 until fftSize) {
                        val c = cosTable[k * fftSize + n]
                        val s = sinTable[k * fftSize + n]
                        r += frame[n] * c
                        i -= frame[n] * s
                    }
                    re[k] = r
                    im[k] = i
                    mag[k] = kotlin.math.sqrt(r * r + i * i)
                    phase[k] = kotlin.math.atan2(i, r)
                }

                // Spectral subtraction algorithm: S_denoised = max(S - alpha * N, beta * S)
                val alpha = 1.6f // Oversubtraction factor
                val beta = 0.04f  // Spectral floor to prevent musical noise artifacts
                val denoisedMag = FloatArray(halfSize + 1)
                for (k in 0..halfSize) {
                    val subbed = mag[k] - alpha * noiseSpectrum[k]
                    denoisedMag[k] = maxOf(subbed, beta * mag[k])
                }

                // Reconstruct Complex Spectrum
                val denoisedRe = FloatArray(halfSize + 1)
                val denoisedIm = FloatArray(halfSize + 1)
                for (k in 0..halfSize) {
                    denoisedRe[k] = denoisedMag[k] * kotlin.math.cos(phase[k])
                    denoisedIm[k] = denoisedMag[k] * kotlin.math.sin(phase[k])
                }

                // Inverse DFT (IDFT)
                val reconstructed = FloatArray(fftSize)
                val invN = 1f / fftSize
                for (n in 0 until fftSize) {
                    var sum = denoisedRe[0]
                    for (k in 1 until halfSize) {
                        val angle = (2.0 * Math.PI * n * k / fftSize).toFloat()
                        val c = kotlin.math.cos(angle)
                        val s = kotlin.math.sin(angle)
                        sum += 2f * (denoisedRe[k] * c - denoisedIm[k] * s)
                    }
                    val angleHalf = (Math.PI * n).toFloat()
                    sum += denoisedRe[halfSize] * kotlin.math.cos(angleHalf)
                    
                    // Re-apply window for seamless synthesis overlap-add
                    reconstructed[n] = (sum * invN) * window[n]
                }

                // Overlap-Add to output accumulator
                for (n in 0 until fftSize) {
                    output[offset + n] += reconstructed[n]
                    windowSum[offset + n] += window[n] * window[n]
                }

                offset += hopSize
            }

            // Normalize and scale back to 16-bit Short integer limits
            val denoisedShorts = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val normFactor = if (windowSum[i] > 1e-4f) windowSum[i] else 1f
                val sample = (output[i] / normFactor) * 32768.0f
                denoisedShorts[i] = sample.coerceIn(Short.MIN_VALUE.toFloat(), Short.MAX_VALUE.toFloat()).toInt().toShort()
            }

            return denoisedShorts
        }
    }
}
