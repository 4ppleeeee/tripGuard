import { ComponentContent } from '@kit.ArkUI';

const DIALOG_TRANS_DURATION_MS = 180;
const DEFAULT_CROP_BOX_SIZE = 320;
const BOTTOM_BAR_HEIGHT = 69;
const BUTTON_SIZE = 44;
const MAX_SCALE = 4;

export interface AvatarCropConfig {
  aspectX: number;
  aspectY: number;
  outputX: number;
  outputY: number;
}

export interface AvatarCropTransform {
  cropBoxSize: number;
  imageDisplayWidth: number;
  imageDisplayHeight: number;
  scale: number;
  offsetX: number;
  offsetY: number;
}

export interface AvatarCropDialogOptions {
  sourceUri: string;
  imageWidth: number;
  imageHeight: number;
  cropConfig: AvatarCropConfig;
}

interface AvatarCropDialogState extends AvatarCropDialogOptions {
  onConfirm: (transform: AvatarCropTransform) => void;
  onCancel: () => void;
  closeDialog?: () => void;
}

export namespace OhosAvatarCropDialog {
  export function open(uiContext: UIContext, options: AvatarCropDialogOptions): Promise<AvatarCropTransform | null> {
    return new Promise<AvatarCropTransform | null>((resolve) => {
      const state: AvatarCropDialogState = {
        sourceUri: options.sourceUri,
        imageWidth: options.imageWidth,
        imageHeight: options.imageHeight,
        cropConfig: options.cropConfig,
        onConfirm: (transform: AvatarCropTransform) => {
          state.closeDialog?.();
          resolve(transform);
        },
        onCancel: () => {
          state.closeDialog?.();
          resolve(null);
        },
      };
      const node = new ComponentContent<AvatarCropDialogState>(uiContext, wrapBuilder(buildAvatarCropDialog), state);
      state.closeDialog = () => uiContext.getPromptAction().closeCustomDialog(node);
      node.update(state);
      uiContext.getPromptAction().openCustomDialog(node, {
        alignment: DialogAlignment.Center,
        maskColor: Color.Black,
        transition: TransitionEffect.OPACITY.animation({ duration: DIALOG_TRANS_DURATION_MS }),
        onWillDismiss: () => {
          resolve(null);
        },
      });
    });
  }
}

@Builder
function buildAvatarCropDialog(state: AvatarCropDialogState) {
  AvatarCropDialogContent({ state })
}

@Component
struct AvatarCropDialogContent {
  state: AvatarCropDialogState = {
    sourceUri: '',
    imageWidth: 1,
    imageHeight: 1,
    cropConfig: {
      aspectX: 1,
      aspectY: 1,
      outputX: 512,
      outputY: 512,
    },
    onConfirm: () => {
    },
    onCancel: () => {
    },
  };
  @State private cropScale: number = 1;
  @State private offsetX: number = 0;
  @State private offsetY: number = 0;
  @State private lastScale: number = 1;
  @State private panStartOffsetX: number = 0;
  @State private panStartOffsetY: number = 0;
  @State private imageDisplayWidth: number = DEFAULT_CROP_BOX_SIZE;
  @State private imageDisplayHeight: number = DEFAULT_CROP_BOX_SIZE;
  private readonly cropBoxSize: number = DEFAULT_CROP_BOX_SIZE;

  aboutToAppear(): void {
    this.initImageDisplaySize();
  }

  private initImageDisplaySize(): void {
    const imageRatio = this.state.imageWidth / this.state.imageHeight;
    const cropRatio = this.state.cropConfig.aspectX / this.state.cropConfig.aspectY;
    if (imageRatio > cropRatio) {
      this.imageDisplayHeight = this.cropBoxSize;
      this.imageDisplayWidth = this.cropBoxSize * imageRatio / cropRatio;
    } else {
      this.imageDisplayWidth = this.cropBoxSize;
      this.imageDisplayHeight = this.cropBoxSize * cropRatio / imageRatio;
    }
    this.clampOffset();
  }

  private handlePanUpdate(event: GestureEvent): void {
    this.offsetX = this.panStartOffsetX + event.offsetX;
    this.offsetY = this.panStartOffsetY + event.offsetY;
    this.clampOffset();
  }

  private handlePanEnd(): void {
    this.panStartOffsetX = this.offsetX;
    this.panStartOffsetY = this.offsetY;
  }

  private handlePinchUpdate(event: GestureEvent): void {
    const nextScale = Math.max(1, Math.min(MAX_SCALE, this.lastScale * event.scale));
    this.cropScale = nextScale;
    this.clampOffset();
  }

  private handlePinchEnd(): void {
    this.lastScale = this.cropScale;
  }

  private clampOffset(): void {
    const displayWidth = this.imageDisplayWidth * this.cropScale;
    const displayHeight = this.imageDisplayHeight * this.cropScale;
    const maxOffsetX = Math.max(0, (displayWidth - this.cropBoxSize) / 2);
    const maxOffsetY = Math.max(0, (displayHeight - this.cropBoxSize) / 2);
    this.offsetX = Math.max(-maxOffsetX, Math.min(maxOffsetX, this.offsetX));
    this.offsetY = Math.max(-maxOffsetY, Math.min(maxOffsetY, this.offsetY));
  }

  private confirmCrop(): void {
    this.state.onConfirm({
      cropBoxSize: this.cropBoxSize,
      imageDisplayWidth: this.imageDisplayWidth,
      imageDisplayHeight: this.imageDisplayHeight,
      scale: this.cropScale,
      offsetX: this.offsetX,
      offsetY: this.offsetY,
    });
  }

  build() {
    Stack() {
      Column() {
        Blank().layoutWeight(1);
        Stack() {
          Image(this.state.sourceUri)
            .width(this.imageDisplayWidth)
            .height(this.imageDisplayHeight)
            .objectFit(ImageFit.Fill)
            .scale({ x: this.cropScale, y: this.cropScale })
            .translate({ x: this.offsetX, y: this.offsetY })
          Rect()
            .width(this.cropBoxSize)
            .height(this.cropBoxSize)
            .fill(Color.Transparent)
            .stroke(Color.White)
            .strokeWidth(1)
        }
        .width(this.cropBoxSize)
        .height(this.cropBoxSize)
        .clip(true)
        .gesture(
          GestureGroup(
            GestureMode.Parallel,
            PanGesture()
              .onActionUpdate((event: GestureEvent) => {
                this.handlePanUpdate(event);
              })
              .onActionEnd(() => {
                this.handlePanEnd();
              }),
            PinchGesture({ fingers: 2 })
              .onActionUpdate((event: GestureEvent) => {
                this.handlePinchUpdate(event);
              })
              .onActionEnd(() => {
                this.handlePinchEnd();
              }),
          ),
        )
        Blank().layoutWeight(1);
        this.BottomBar();
      }
      .width('100%')
      .height('100%')
      .backgroundColor(Color.Black)
    }
    .width('100%')
    .height('100%')
    .backgroundColor(Color.Black)
  }

  @Builder
  private BottomBar() {
    Row() {
      Text('×')
        .fontColor(Color.White)
        .fontSize(32)
        .textAlign(TextAlign.Center)
        .width(BUTTON_SIZE)
        .height(BUTTON_SIZE)
        .onClick(() => {
          this.state.onCancel();
        })
      Blank().layoutWeight(1)
      Text('还原')
        .fontColor('#B3FFFFFF')
        .fontSize(16)
        .textAlign(TextAlign.Center)
        .width(64)
        .height(BUTTON_SIZE)
        .onClick(() => {
          this.cropScale = 1;
          this.lastScale = 1;
          this.offsetX = 0;
          this.offsetY = 0;
          this.panStartOffsetX = 0;
          this.panStartOffsetY = 0;
        })
      Blank().layoutWeight(1)
      Text('✓')
        .fontColor(Color.White)
        .fontSize(30)
        .textAlign(TextAlign.Center)
        .width(BUTTON_SIZE)
        .height(BUTTON_SIZE)
        .onClick(() => {
          this.confirmCrop();
        })
    }
    .width('100%')
    .height(BOTTOM_BAR_HEIGHT)
    .padding({ left: 12, right: 12 })
  }
}
