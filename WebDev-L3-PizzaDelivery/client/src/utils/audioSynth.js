// High-end Web Audio API Synthesizer for Woodfire Kitchen Ambiance & Tactile Clicks
// 100% standalone — zero external MP3 dependencies, never fails on offline/network errors.

class KitchenAudioSynthesizer {
  constructor() {
    this.ctx = null;
    this.isSizzlePlaying = false;
    this.sizzleGain = null;
    this.noiseNode = null;
    this.crackleTimer = null;
  }

  init() {
    if (!this.ctx) {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      if (AudioContext) {
        this.ctx = new AudioContext();
      }
    }
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  // Subtle tactile tap when user selects an ingredient
  playIngredientClick() {
    try {
      this.init();
      if (!this.ctx) return;

      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(580, this.ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(320, this.ctx.currentTime + 0.04);

      gain.gain.setValueAtTime(0.12, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.04);

      osc.connect(gain);
      gain.connect(this.ctx.destination);

      osc.start();
      osc.stop(this.ctx.currentTime + 0.04);
    } catch (e) {
      // Audio autoplay policy fallback
    }
  }

  // Soft high-frequency sizzle drop when cheese / topping is added
  playToppingPop() {
    try {
      this.init();
      if (!this.ctx) return;

      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();

      osc.type = 'triangle';
      osc.frequency.setValueAtTime(880, this.ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(1240, this.ctx.currentTime + 0.06);

      gain.gain.setValueAtTime(0.08, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.06);

      osc.connect(gain);
      gain.connect(this.ctx.destination);

      osc.start();
      osc.stop(this.ctx.currentTime + 0.06);
    } catch (e) {
      // Audio autoplay policy fallback
    }
  }

  // Continuous 450°C woodfire oven crackle & sizzle ambience
  toggleWoodfireSizzle(enable) {
    this.init();
    if (!this.ctx) return false;

    if (!enable) {
      if (this.sizzleGain) {
        this.sizzleGain.gain.linearRampToValueAtTime(0.001, this.ctx.currentTime + 0.5);
      }
      if (this.crackleTimer) {
        clearInterval(this.crackleTimer);
        this.crackleTimer = null;
      }
      setTimeout(() => {
        if (this.noiseNode) {
          try {
            this.noiseNode.stop();
            this.noiseNode.disconnect();
          } catch {}
          this.noiseNode = null;
        }
      }, 500);
      this.isSizzlePlaying = false;
      return false;
    }

    // Start Sizzle White Noise
    const bufferSize = this.ctx.sampleRate * 2;
    const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
    const data = buffer.getChannelData(0);
    for (let i = 0; i < bufferSize; i++) {
      data[i] = Math.random() * 2 - 1;
    }

    this.noiseNode = this.ctx.createBufferSource();
    this.noiseNode.buffer = buffer;
    this.noiseNode.loop = true;

    // Filter to soft hiss (simulating olive oil & cheese sizzling on hot stone)
    const bandpass = this.ctx.createBiquadFilter();
    bandpass.type = 'bandpass';
    bandpass.frequency.value = 3200;
    bandpass.Q.value = 0.8;

    this.sizzleGain = this.ctx.createGain();
    this.sizzleGain.gain.setValueAtTime(0.001, this.ctx.currentTime);
    this.sizzleGain.gain.linearRampToValueAtTime(0.06, this.ctx.currentTime + 0.6);

    this.noiseNode.connect(bandpass);
    bandpass.connect(this.sizzleGain);
    this.sizzleGain.connect(this.ctx.destination);

    this.noiseNode.start();

    // Woodfire snap / crackle pops at randomized intervals
    this.crackleTimer = setInterval(() => {
      if (!this.isSizzlePlaying || !this.ctx) return;
      if (Math.random() > 0.4) {
        try {
          const pop = this.ctx.createOscillator();
          const popGain = this.ctx.createGain();
          pop.type = 'sawtooth';
          pop.frequency.setValueAtTime(140 + Math.random() * 180, this.ctx.currentTime);

          popGain.gain.setValueAtTime(0.05 + Math.random() * 0.08, this.ctx.currentTime);
          popGain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.03);

          pop.connect(popGain);
          popGain.connect(this.ctx.destination);

          pop.start();
          pop.stop(this.ctx.currentTime + 0.03);
        } catch {}
      }
    }, 280);

    this.isSizzlePlaying = true;
    return true;
  }
}

export const kitchenAudio = new KitchenAudioSynthesizer();
