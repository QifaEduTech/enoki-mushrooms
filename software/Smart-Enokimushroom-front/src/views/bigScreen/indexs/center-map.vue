<template>
<div class="centermap">
    <div class="maptitle">
        <div class="title-deco left-deco">
            <span class="deco-line"></span>
            <span class="deco-dot"></span>
        </div>
        <span class="titletext">金针菇状态直播</span>
        <div class="title-deco right-deco">
            <span class="deco-dot"></span>
            <span class="deco-line"></span>
        </div>
    </div>
    <div class="video-container">
        <div class="video-inner">
            <Player :appKey="appKey" :appSecret="appSecret" :cameraId="cameraId" />
        </div>
    </div>
</div>
</template>

<script>
import Player from "../components/Player.vue";

export default {
    components: { Player },
    data() {
        return {
            appKey: '85641fca6aa54bf7a201d7b17b393b20',
            appSecret: '028be1a965b2ff3d695c6282029ae2c1',
            cameraId: 'BG8921310',
        };
    },
};
</script>

<style lang="scss">
.centermap {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;

    .maptitle {
        height: 50px;
        display: flex;
        justify-content: center;
        align-items: center;
        padding-top: 4px;
        box-sizing: border-box;
        gap: 16px;

        .titletext {
            font-size: 26px;
            font-weight: 800;
            letter-spacing: 8px;
            background: linear-gradient(92deg, #4da4ff 0%, #00eaff 45%, #01aaff 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            white-space: nowrap;
        }

        .title-deco {
            display: flex;
            align-items: center;
            gap: 6px;

            .deco-line {
                width: 60px;
                height: 1px;
                background: linear-gradient(90deg, transparent, rgba(0, 234, 255, 0.4));
            }

            .deco-dot {
                width: 6px;
                height: 6px;
                background: rgba(0, 234, 255, 0.6);
                transform: rotate(45deg);
            }

            &.right-deco {
                .deco-line {
                    background: linear-gradient(90deg, rgba(0, 234, 255, 0.4), transparent);
                }
            }
        }
    }

    .video-container {
        flex: 1;
        background: rgba(6, 22, 56, 0.4);
        border: 1px solid rgba(0, 114, 255, 0.15);
        border-radius: 4px;
        padding: 8px;
        box-sizing: border-box;
        position: relative;

        &::before {
            content: '';
            position: absolute;
            top: -1px;
            left: 20%;
            right: 20%;
            height: 1px;
            background: linear-gradient(90deg, transparent, rgba(0, 234, 255, 0.3), transparent);
        }

        .video-inner {
            width: 100%;
            height: 100%;
            border-radius: 2px;
            overflow: hidden;
        }
    }
}
</style>
