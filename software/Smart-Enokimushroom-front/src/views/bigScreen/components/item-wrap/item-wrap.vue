<template>
<div class="card-wrap">
    <div class="card-border">
        <!-- 四角装饰 -->
        <span class="corner corner-tl"></span>
        <span class="corner corner-tr"></span>
        <span class="corner corner-bl"></span>
        <span class="corner corner-br"></span>
        <!-- 标题 -->
        <div class="card-header" v-if="title !== ''">
            <div class="header-line"></div>
            <div class="header-icon">◆</div>
            <span class="title-text">{{ title }}</span>
            <div class="header-icon">◆</div>
            <div class="header-line"></div>
        </div>
        <!-- 内容 -->
        <div :class="title !== '' ? 'card-body' : 'card-body-full'">
            <slot></slot>
        </div>
    </div>
</div>
</template>

<script>
export default {
    data() {
        return {};
    },
    props: {
        title: {
            type: String,
            default: () => "",
        },
    },
};
</script>

<style lang="scss" scoped>
$card-bg: rgba(6, 22, 56, 0.55);
$border-color: rgba(0, 114, 255, 0.2);
$glow-color: rgba(0, 234, 255, 0.08);
$title-height: 40px;

.card-wrap {
    width: 100%;
    height: 100%;
    position: relative;
}

.card-border {
    width: 100%;
    height: 100%;
    box-sizing: border-box;
    background: $card-bg;
    border: 1px solid $border-color;
    border-radius: 4px;
    backdrop-filter: blur(8px);
    position: relative;
    overflow: hidden;
    transition: border-color 0.3s;

    &:hover {
        border-color: rgba(0, 234, 255, 0.3);
    }

    // 背景微光
    &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 1px;
        background: linear-gradient(90deg, transparent, rgba(0, 234, 255, 0.2), transparent);
    }
}

// ==================== 四角装饰 ====================
.corner {
    position: absolute;
    width: 14px;
    height: 14px;
    z-index: 2;

    &::before,
    &::after {
        content: '';
        position: absolute;
        background: rgba(0, 234, 255, 0.6);
    }

    &.corner-tl {
        top: -1px;
        left: -1px;
        &::before { width: 14px; height: 2px; top: 0; left: 0; }
        &::after { width: 2px; height: 14px; top: 0; left: 0; }
    }

    &.corner-tr {
        top: -1px;
        right: -1px;
        &::before { width: 14px; height: 2px; top: 0; right: 0; }
        &::after { width: 2px; height: 14px; top: 0; right: 0; }
    }

    &.corner-bl {
        bottom: -1px;
        left: -1px;
        &::before { width: 14px; height: 2px; bottom: 0; left: 0; }
        &::after { width: 2px; height: 14px; bottom: 0; left: 0; }
    }

    &.corner-br {
        bottom: -1px;
        right: -1px;
        &::before { width: 14px; height: 2px; bottom: 0; right: 0; }
        &::after { width: 2px; height: 14px; bottom: 0; right: 0; }
    }
}

// ==================== 标题栏 ====================
.card-header {
    height: $title-height;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 0 16px;
    position: relative;

    .header-line {
        flex: 1;
        height: 1px;
        background: linear-gradient(90deg, transparent, rgba(0, 234, 255, 0.25), transparent);
    }

    .header-icon {
        color: rgba(0, 234, 255, 0.5);
        font-size: 8px;
        animation: icon-pulse 3s ease-in-out infinite;
    }

    .title-text {
        font-size: 16px;
        font-weight: 700;
        letter-spacing: 4px;
        background: linear-gradient(92deg, #4da4ff 0%, #00eaff 50%, #01aaff 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        white-space: nowrap;
    }
}

// ==================== 内容区 ====================
.card-body {
    height: calc(100% - #{$title-height});
    padding: 4px 12px 10px;
    box-sizing: border-box;
}

.card-body-full {
    width: 100%;
    height: 100%;
    padding: 8px;
    box-sizing: border-box;
}

@keyframes icon-pulse {
    0%, 100% { opacity: 0.4; }
    50% { opacity: 1; }
}
</style>
