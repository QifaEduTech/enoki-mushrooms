<template>
  <div class="ezuikit-js">
    <div>
      <div id="video-container" style="height: 400px"></div>
    </div>
    <div>
      <button v-on:click="init">init</button>
      <button v-on:click="stop">stop</button>
      <button v-on:click="play">play</button>
      <button v-on:click="openSound">openSound</button>
      <button v-on:click="closeSound">closeSound</button>
      <button v-on:click="startSave">startSave</button>
      <button v-on:click="stopSave">stopSave</button>
      <button v-on:click="capturePicture">capturePicture</button>
      <button v-on:click="fullscreen">fullscreen</button>
      <button v-on:click="getOSDTime">getOSDTime</button>
      <button v-on:click="startTalk">startTalk</button>
      <button v-on:click="stopTalk">stopTalk</button>
      <button v-on:click="destroy">destroy</button>
    </div>
  </div>
</template>

<script>
import { EZUIKitPlayer } from 'ezuikit-js/ezuikit.js';
var player = null;

export default {
  name: 'Player',
  props: {
    appKey: String,
    appSecret: String,
    cameraId: String,
    accessToken: String,
  },
  data() {
    return {
      token: this.accessToken || '',
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    async init() {
      if (player) {
        this.destroy();
      }
      try {
        if (!this.token && this.appKey && this.appSecret) {
          await this.fetchAccessToken();
        }
        const url = this.cameraId ? `ezopen://open.ys7.com/${this.cameraId}/1.hd.live` : undefined;
        player = new EZUIKitPlayer({
          id: 'video-container',
          accessToken: this.token,
          url,
          template: 'pcLive',
          height: 400,
          handleError: (error) => {
            console.error('handleError', error);
          },
          language: 'zh',
          decoderType: 'auto',
          scaleMode: 1,
          env: { domain: 'https://open.ys7.com' },
          loggerOptions: { level: 'INFO', name: 'ezuikit', showTime: true },
          streamInfoCBType: 1,
        });
      } catch (e) {
        console.error('初始化播放器失败', e);
      }

      window.player = player;
    },
    async fetchAccessToken() {
      const res = await fetch('https://open.ys7.com/api/lapp/token/get', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ appKey: this.appKey, appSecret: this.appSecret }),
      });
      const data = await res.json();
      if (data && data.code === '200') {
        this.token = data.data.accessToken;
      } else {
        throw new Error('获取AccessToken失败');
      }
    },
    play() {
      if(player)
        player.play().then((data) => {
          console.log("play 获取 数据", data);
        });
    },
    stop() {
      if(player)
        player.stop().then((data) => {
          console.log("stop 获取 数据", data);
        });
    },
    getOSDTime() {
      if(player)
      player.getOSDTime().then((data) => {
        console.log("getOSDTime 获取 数据", data);
      });
    },
    capturePicture() {
      if(player)
      player.capturePicture(
        `${new Date().getTime()}`
      ).then((data) => {
        console.log("capturePicture 获取 数据", data);
      });
    },
    openSound() {
      if(player) player.openSound()
    },
    closeSound() {
      if(player) player.closeSound()
    },
    startSave() {
      if(player)
        player.startSave(`${new Date().getTime()}`).then((data) => {
          console.log("startSave 获取 数据", data);
        });
    },
    stopSave() {
      if(player)
        player.stopSave().then((data) => {
          console.log("promise 获取 数据", data);
        });
    },
    startTalk() {
      if(player) player.startTalk();
    },
    stopTalk() {
      if(player) player.stopTalk();
    },
    fullscreen() {
      if(player) player.fullscreen(); // 8.2.0 新增, 老版本使用 fullScreen()
    },
    destroy() {
      if(player) {
        player.destroy().then((data) => {
          console.log("promise 获取 数据", data);
        });
        player = null;
      }
    },
  },
};
</script>
