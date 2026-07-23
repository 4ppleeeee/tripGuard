import { common } from '@kit.AbilityKit';
import { http } from '@kit.NetworkKit';
import { camera, cameraPicker } from '@kit.CameraKit';
import { fileIo, fileUri } from '@kit.CoreFileKit';
import { pasteboard } from '@kit.BasicServicesKit';
import { photoAccessHelper } from '@kit.MediaLibraryKit';
import { image } from '@kit.ImageKit';
import { effectKit } from '@kit.ArkGraphics2D';
import { promptAction } from '@kit.ArkUI';
import { util } from '@kit.ArkTS';
import hilog from '@ohos.hilog';

const TAG = 'OhosAppResManager';
const DOMAIN = 0x0000;
const DEFAULT_COLOR_HEX = '#000000';
const CODE_UNSUPPORTED = -30000;

/**
 * 鸿蒙端 IResManager 回调实现。
 * 对应 KMP 侧 @KNCallback 接口 OhosAppResManager。
 *
 * 真实使用鸿蒙系统 API：
 * - getAssetJson：resourceManager.getRawFileContent 读取 rawfile
 * - copyToClipboard：pasteboard 系统剪贴板
 * - preloadImage / preloadAlphaVideo / preloadLottieToMemory：http 下载到沙盒 cacheDir
 * - saveImage / saveVideo：photoAccessHelper.createAsset 写入系统相册
 * - selectImage：photoAccessHelper.PhotoViewPicker 拉起相册
 * - getPaletteColor：effectKit.createColorPicker 对图片 pixelMap 取主色
 */
export class OhosAppResManagerCallback {
  private readonly context: common.UIAbilityContext;
  private readonly mediaResults: Map<number, string> = new Map<number, string>();
  private mediaResultToken: number = 1;

  constructor(context: common.UIAbilityContext) {
    this.context = context;
  }

  // region rawfile 资源读取

  getAssetJson(fileName: string): string {
    if (!fileName) {
      return '';
    }
    try {
      const content = this.context.resourceManager.getRawFileContentSync(fileName);
      const decoder = util.TextDecoder.create('utf-8');
      return decoder.decodeToString(content);
    } catch (err) {
      hilog.error(DOMAIN, TAG, 'getAssetJson failed, fileName=%{public}s, err=%{public}s',
        fileName, JSON.stringify(err) ?? '');
      return '';
    }
  }

  // endregion

  // region 剪贴板

  copyToClipboard(content: string): void {
    if (!content) {
      return;
    }
    try {
      const pasteData = pasteboard.createData(pasteboard.MIMETYPE_TEXT_PLAIN, content);
      pasteboard.getSystemPasteboard().setData(pasteData).catch((err: Object) => {
        hilog.error(DOMAIN, TAG, 'copyToClipboard setData failed, err=%{public}s',
          JSON.stringify(err) ?? '');
      });
    } catch (err) {
      hilog.error(DOMAIN, TAG, 'copyToClipboard failed, err=%{public}s', JSON.stringify(err) ?? '');
    }
  }

  // endregion

  // region 图片预加载（缓存到沙盒，后续图片库可直接复用）

  preloadImage(url: string, onResult: (success: boolean) => void): void {
    if (!url || !url.startsWith('http')) {
      onResult(false);
      return;
    }
    const cachePath = this.buildCachePath('preload_img', url);
    if (fileIo.accessSync(cachePath)) {
      onResult(true);
      return;
    }
    this.downloadToFile(url, cachePath)
      .then(() => onResult(true))
      .catch((err: Object) => {
        hilog.error(DOMAIN, TAG, 'preloadImage failed, url=%{public}s, err=%{public}s',
          url, JSON.stringify(err) ?? '');
        onResult(false);
      });
  }

  // endregion

  // region 保存图片到系统相册

  saveImage(url: string, onResult: (success: boolean, message: string) => void): void {
    if (!url) {
      onResult(false, '图片地址为空');
      return;
    }
    const extension = this.guessExtension(url, 'jpg', ['jpg', 'jpeg', 'png', 'webp', 'gif']);
    this.prepareLocalFile(url, 'save_img', extension)
      .then((localPath) => this.saveMediaToAlbum(localPath, 'image', extension))
      .then(() => onResult(true, '已保存到相册'))
      .catch((err: Object) => {
        hilog.error(DOMAIN, TAG, 'saveImage failed, url=%{public}s, err=%{public}s',
          url, JSON.stringify(err) ?? '');
        onResult(false, '保存失败，请重试');
      });
  }

  // endregion

  // region 保存视频到系统相册

  saveVideo(localFilePath: string): void {
    const extension = this.guessExtension(localFilePath, 'mp4', ['mp4', 'mov', 'm4v']);
    this.saveVideoToAlbumViaDialog(localFilePath, extension)
      .then(() => {
        hilog.error(DOMAIN, TAG, "保存成功")
      })
      .catch((err: Object) => {
        hilog.error(DOMAIN, TAG, 'saveVideo failed, path=%{public}s, err=%{public}s',
          localFilePath, JSON.stringify(err) ?? '');
      });
  }

  /**
   * 通过 showAssetsCreationDialog 弹窗授权方式保存视频到相册。
   * 无需申请 ohos.permission.WRITE_IMAGEVIDEO 权限，用户确认弹窗即可完成保存。
   * 参考文档：https://developer.huawei.com/consumer/cn/doc/architecture-guides/tools-v1_2-ts_283-0000002429991637
   */
  private async saveVideoToAlbumViaDialog(localFilePath: string, extension: string): Promise<void> {
    const helper = photoAccessHelper.getPhotoAccessHelper(this.context);

    // 1. 构建源文件 uri 列表
    const srcFileUri_: string = fileUri.getUriFromPath(localFilePath);
    const srcFileUris: Array<string> = [srcFileUri_];

    // 2. 构建创建配置（指定文件后缀和媒体类型）
    const photoCreationConfigs: Array<photoAccessHelper.PhotoCreationConfig> = [{
      title: `kmm_video_${Date.now()}`,
      fileNameExtension: extension,
      photoType: photoAccessHelper.PhotoType.VIDEO,
      subtype: photoAccessHelper.PhotoSubtype.DEFAULT,
    }];

    // 3. 弹窗授权，获取目标媒体库 uri
    const desFileUris: Array<string> = await helper.showAssetsCreationDialog(srcFileUris, photoCreationConfigs);
    if (!desFileUris || desFileUris.length === 0) {
      throw new Error('用户取消保存或授权失败');
    }

    // 4. 将源文件内容写入目标 uri
    const destUri = desFileUris[0];
    const srcFile = fileIo.openSync(localFilePath, fileIo.OpenMode.READ_ONLY);
    const dstFile = fileIo.openSync(destUri, fileIo.OpenMode.WRITE_ONLY);
    try {
      fileIo.copyFileSync(srcFile.fd, dstFile.fd);
    } finally {
      fileIo.closeSync(srcFile);
      fileIo.closeSync(dstFile);
    }
  }

  // endregion

  // region 相册选图

  selectImage(maxCount: number, onResult: (resultToken: number) => void): void {
    this.selectMedia(1, maxCount, false, 1, 1, 1, 1, onResult);
  }

  /**
   * 展示媒体来源选择菜单。
   * mediaType: 1-图片，2-视频，3-图片+视频
   */
  selectMediaSource(
    mediaType: number,
    maxCount: number,
    cropEnabled: boolean,
    aspectX: number,
    aspectY: number,
    outputX: number,
    outputY: number,
    onResult: (resultToken: number) => void,
  ): void {
    if (mediaType !== 1) {
      this.selectMedia(mediaType, maxCount, cropEnabled, aspectX, aspectY, outputX, outputY, onResult);
      return;
    }
    promptAction.showActionMenu({
      buttons: [
        { text: '从相册选择', color: '#111111' },
        { text: '拍照', color: '#111111' },
      ],
    })
      .then((result: promptAction.ActionMenuSuccessResponse) => {
        if (result.index === 0) {
          this.selectMedia(mediaType, maxCount, cropEnabled, aspectX, aspectY, outputX, outputY, onResult);
          return;
        }
        if (result.index === 1) {
          this.takePhoto(cropEnabled, aspectX, aspectY, outputX, outputY, onResult);
          return;
        }
        onResult(this.createMediaResultToken(''));
      })
      .catch((err: Object) => {
        hilog.error(DOMAIN, TAG, 'selectMediaSource failed, err=%{public}s', JSON.stringify(err) ?? '');
        onResult(this.createMediaResultToken(''));
      });
  }

  /**
   * 拉起系统相机拍照。
   */
  takePhoto(
    cropEnabled: boolean,
    aspectX: number,
    aspectY: number,
    outputX: number,
    outputY: number,
    onResult: (resultToken: number) => void,
  ): void {
    const pickerProfile: cameraPicker.PickerProfile = {
      cameraPosition: camera.CameraPosition.CAMERA_POSITION_BACK,
    };
    cameraPicker.pick(this.context, [cameraPicker.PickerMediaType.PHOTO], pickerProfile)
      .then((result: cameraPicker.PickerResult) => {
        const resultUri = result.resultUri ?? '';
        console.info(`OhosAppResManager takePhoto resultUri=${resultUri}`);
        this.preparePickedMediaResults(
          resultUri.length > 0 ? [resultUri] : [],
        )
          .then((paths: string[]) => {
            const encodedResult = this.encodeMediaResult(paths);
            console.info(`OhosAppResManager takePhoto preparedCount=${paths.length} encodedResult=${encodedResult}`);
            onResult(this.createMediaResultToken(encodedResult));
          })
          .catch((err: Object) => {
            console.info(`OhosAppResManager takePhoto prepare failed err=${JSON.stringify(err) ?? ''}`);
            hilog.error(DOMAIN, TAG, 'takePhoto prepare failed, err=%{public}s', JSON.stringify(err) ?? '');
            onResult(this.createMediaResultToken(''));
          });
      })
      .catch((err: Object) => {
        console.info(`OhosAppResManager takePhoto failed err=${JSON.stringify(err) ?? ''}`);
        hilog.error(DOMAIN, TAG, 'takePhoto failed, err=%{public}s', JSON.stringify(err) ?? '');
        onResult(this.createMediaResultToken(''));
      });
  }

  /**
   * 拉起系统相册选择媒体。
   * mediaType: 1-图片，2-视频，3-图片+视频
   */
  selectMedia(
    mediaType: number,
    maxCount: number,
    cropEnabled: boolean,
    aspectX: number,
    aspectY: number,
    outputX: number,
    outputY: number,
    onResult: (resultToken: number) => void,
  ): void {
    try {
      const options = new photoAccessHelper.PhotoSelectOptions();
      if (mediaType === 2) {
        options.MIMEType = photoAccessHelper.PhotoViewMIMETypes.VIDEO_TYPE;
      } else if (mediaType === 3) {
        options.MIMEType = photoAccessHelper.PhotoViewMIMETypes.IMAGE_VIDEO_TYPE;
      } else {
        options.MIMEType = photoAccessHelper.PhotoViewMIMETypes.IMAGE_TYPE;
      }
      options.maxSelectNumber = maxCount > 0 ? maxCount : 1;
      const picker = new photoAccessHelper.PhotoViewPicker();
      picker.select(options)
        .then((result: photoAccessHelper.PhotoSelectResult) => {
          const photoUris = result.photoUris ?? [];
          console.info(`OhosAppResManager selectMedia photoUriCount=${photoUris.length} photoUris=${photoUris.join(',')}`);
          this.preparePickedMediaResults(
            photoUris,
          )
            .then((paths: string[]) => {
              const encodedResult = this.encodeMediaResult(paths);
              console.info(`OhosAppResManager selectMedia preparedCount=${paths.length} encodedResult=${encodedResult}`);
              onResult(this.createMediaResultToken(encodedResult));
            })
            .catch((err: Object) => {
              console.info(`OhosAppResManager selectMedia prepare failed err=${JSON.stringify(err) ?? ''}`);
              hilog.error(DOMAIN, TAG, 'selectMedia prepare failed, err=%{public}s', JSON.stringify(err) ?? '');
              onResult(this.createMediaResultToken(''));
            });
        })
        .catch((err: Object) => {
          console.info(`OhosAppResManager selectMedia failed mediaType=${mediaType} err=${JSON.stringify(err) ?? ''}`);
          hilog.error(DOMAIN, TAG, 'selectMedia failed, mediaType=%{public}d, err=%{public}s',
            mediaType, JSON.stringify(err) ?? '');
          onResult(this.createMediaResultToken(''));
        });
    } catch (err) {
      console.info(`OhosAppResManager selectMedia threw mediaType=${mediaType} err=${JSON.stringify(err) ?? ''}`);
      hilog.error(DOMAIN, TAG, 'selectMedia threw, mediaType=%{public}d, err=%{public}s',
        mediaType, JSON.stringify(err) ?? '');
      onResult(this.createMediaResultToken(''));
    }
  }

  consumeMediaResult(resultToken: number): string {
    const result = this.mediaResults.get(resultToken) ?? '';
    console.info(`OhosAppResManager consumeMediaResult token=${resultToken} result=${result} cacheSizeBefore=${this.mediaResults.size}`);
    if (result.length > 0 || this.mediaResults.has(resultToken)) {
      this.mediaResults.delete(resultToken);
      console.info(`OhosAppResManager consumeMediaResult token=${resultToken} cacheSizeAfter=${this.mediaResults.size}`);
      return result;
    }
    if (this.mediaResults.size === 1) {
      const fallbackToken = this.mediaResults.keys().next().value as number;
      const fallbackResult = this.mediaResults.get(fallbackToken) ?? '';
      console.info(
        `OhosAppResManager consumeMediaResult fallback requestedToken=${resultToken} ` +
          `fallbackToken=${fallbackToken} result=${fallbackResult}`,
      );
      this.mediaResults.delete(fallbackToken);
      console.info(`OhosAppResManager consumeMediaResult fallback cacheSizeAfter=${this.mediaResults.size}`);
      return fallbackResult;
    }
    console.info(`OhosAppResManager consumeMediaResult token=${resultToken} cacheSizeAfter=${this.mediaResults.size}`);
    return '';
  }

  private createMediaResultToken(mediaResult: string): number {
    const resultToken = this.mediaResultToken;
    this.mediaResultToken += 1;
    this.mediaResults.set(resultToken, mediaResult);
    console.info(`OhosAppResManager createMediaResultToken token=${resultToken} result=${mediaResult} cacheSize=${this.mediaResults.size}`);
    return resultToken;
  }

  private async preparePickedMediaResults(urls: string[]): Promise<string[]> {
    const paths: string[] = [];
    for (const url of urls) {
      if (url.length === 0) {
        continue;
      }
      try {
        const path = this.copyMediaToLocalFile(url);
        console.info(`OhosAppResManager prepare media copied source=${url} path=${path}`);
        if (this.isValidLocalFile(path)) {
          paths.push(path);
        }
      } catch (err) {
        console.info(`OhosAppResManager prepare media failed uri=${url} err=${JSON.stringify(err) ?? ''}`);
        hilog.error(DOMAIN, TAG, 'prepare media failed, uri=%{public}s, err=%{public}s', url, JSON.stringify(err) ?? '');
      }
    }
    hilog.info(DOMAIN, TAG, 'preparePickedMediaResults source count=%{public}d, result count=%{public}d',
      urls.length, paths.length);
    return paths;
  }

  private copyMediaToLocalFile(sourceUri: string): string {
    const extension = this.guessExtension(sourceUri, 'jpg', ['jpg', 'jpeg', 'png', 'webp', 'gif', 'mp4', 'mov', 'm4v']);
    const outputPath = this.buildPickedMediaPath('picked_media', extension);
    const sourcePath = sourceUri;
    const sourceFile = fileIo.openSync(sourcePath, fileIo.OpenMode.READ_ONLY);
    const outputFile = fileIo.openSync(outputPath, fileIo.OpenMode.CREATE | fileIo.OpenMode.WRITE_ONLY | fileIo.OpenMode.TRUNC);
    try {
      fileIo.copyFileSync(sourceFile.fd, outputFile.fd);
    } finally {
      fileIo.closeSync(sourceFile);
      fileIo.closeSync(outputFile);
    }
    if (!this.isValidLocalFile(outputPath)) {
      throw new Error(`copied media file invalid: ${outputPath}`);
    }
    return outputPath;
  }

  private isValidLocalFile(path: string): boolean {
    if (path.length === 0 || !fileIo.accessSync(path)) {
      return false;
    }
    return fileIo.statSync(path).size > 0;
  }

  private buildPickedMediaPath(prefix: string, extension: string): string {
    const dirPath = `${this.context.cacheDir}/kmm_picker`;
    if (!fileIo.accessSync(dirPath)) {
      fileIo.mkdirSync(dirPath, true);
    }
    return `${dirPath}/${prefix}_${Date.now()}.${extension}`;
  }

  private encodeMediaResult(urls: string[]): string {
    if (urls.length === 0) {
      return '';
    }
    return urls.filter((url: string) => url.length > 0).join('\n');
  }

  // endregion

  // region 媒体上传

  uploadImage(
    serialNo: number,
    filePath: string,
    scene: number,
    authJson: string,
    onProgress: (current: number, total: number) => void,
    onResult: (code: number, message: string, url: string, fileId: string) => void,
  ): void {
    hilog.warn(DOMAIN, TAG, 'uploadImage ignored: upload flow is not part of the demo shell');
    onProgress(0, 0);
    onResult(CODE_UNSUPPORTED, 'upload is not supported in demo shell', '', '');
  }

  uploadVideo(
    serialNo: number,
    filePath: string,
    authJson: string,
    onProgress: (current: number, total: number) => void,
    onResult: (code: number, message: string, videoId: string, fileId: string) => void,
  ): void {
    hilog.warn(DOMAIN, TAG, 'uploadVideo ignored: upload flow is not part of the demo shell');
    onProgress(0, 0);
    onResult(CODE_UNSUPPORTED, 'upload is not supported in demo shell', '', '');
  }

  // endregion

  // region 取色

  getPaletteColor(imageUrl: string, paletteParam: string, onResult: (color: string) => void): void {
    if (!imageUrl) {
      onResult(DEFAULT_COLOR_HEX);
      return;
    }
    let parsedParam: PaletteParam | null = null;
    try {
      parsedParam = JSON.parse(paletteParam) as PaletteParam;
    } catch (err) {
      hilog.error(DOMAIN, TAG, 'getPaletteColor parse param failed, err=%{public}s',
        JSON.stringify(err) ?? '');
    }
    this.prepareLocalFile(imageUrl, 'palette_img', this.guessExtension(imageUrl, 'jpg', ['jpg', 'jpeg', 'png', 'webp']))
      .then((localPath) => this.pickMainColorFromFile(localPath, parsedParam))
      .then((color) => onResult(color))
      .catch((err: Object) => {
        hilog.error(DOMAIN, TAG, 'getPaletteColor failed, url=%{public}s, err=%{public}s',
          imageUrl, JSON.stringify(err) ?? '');
        onResult(DEFAULT_COLOR_HEX);
      });
  }

  // endregion

  // region Lottie / Alpha 视频预加载

  preloadLottieToMemory(url: string, onResult: (success: boolean) => void): void {
    if (!url || !url.startsWith('http')) {
      onResult(false);
      return;
    }
    const cachePath = this.buildCachePath('preload_lottie', url);
    if (fileIo.accessSync(cachePath)) {
      onResult(true);
      return;
    }
    this.downloadToFile(url, cachePath)
      .then(() => onResult(true))
      .catch((err: Object) => {
        hilog.error(DOMAIN, TAG, 'preloadLottieToMemory failed, url=%{public}s, err=%{public}s',
          url, JSON.stringify(err) ?? '');
        onResult(false);
      });
  }

  preloadAlphaVideo(url: string, onResult: (success: boolean) => void): void {
    if (!url || !url.startsWith('http')) {
      onResult(false);
      return;
    }
    const cachePath = this.buildCachePath('preload_alpha_video', url);
    if (fileIo.accessSync(cachePath)) {
      onResult(true);
      return;
    }
    this.downloadToFile(url, cachePath)
      .then(() => onResult(true))
      .catch((err: Object) => {
        hilog.error(DOMAIN, TAG, 'preloadAlphaVideo failed, url=%{public}s, err=%{public}s',
          url, JSON.stringify(err) ?? '');
        onResult(false);
      });
  }

  // endregion

  // region 内部辅助：下载 / 缓存 / 相册写入 / 取色

  /**
   * 依据 url 生成带可预测哈希的缓存路径，避免同 url 多次下载。
   * 哈希使用 FNV-1a 32 位，足以做缓存 key，不需要加密强度。
   */
  private buildCachePath(subDir: string, keyContent: string): string {
    const dirPath = `${this.context.cacheDir}/kmm_res/${subDir}`;
    if (!fileIo.accessSync(dirPath)) {
      try {
        fileIo.mkdirSync(dirPath, true);
      } catch (err) {
        hilog.error(DOMAIN, TAG, 'mkdir failed, dir=%{public}s, err=%{public}s',
          dirPath, JSON.stringify(err) ?? '');
      }
    }
    const hash = this.fnv1aHash(keyContent);
    const extMatch = /\.([a-zA-Z0-9]{1,5})(?:\?|#|$)/.exec(keyContent);
    const ext = extMatch ? extMatch[1].toLowerCase() : 'bin';
    return `${dirPath}/${hash}.${ext}`;
  }

  private fnv1aHash(text: string): string {
    let hash = 0x811C9DC5;
    for (let i = 0; i < text.length; i++) {
      hash ^= text.charCodeAt(i);
      // FNV prime = 16777619
      hash = (hash + ((hash << 1) + (hash << 4) + (hash << 7) + (hash << 8) + (hash << 24))) >>> 0;
    }
    return hash.toString(16);
  }

  private guessExtension(url: string, defaultExt: string, allowList: string[]): string {
    const match = /\.([a-zA-Z0-9]{1,5})(?:\?|#|$)/.exec(url);
    if (!match) {
      return defaultExt;
    }
    const ext = match[1].toLowerCase();
    return allowList.indexOf(ext) >= 0 ? ext : defaultExt;
  }

  /**
   * 下载网络文件到指定路径（原子写：先写 .tmp 再 rename）。
   */
  private downloadToFile(url: string, targetPath: string): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      const request = http.createHttp();
      request.request(url, { method: http.RequestMethod.GET, expectDataType: http.HttpDataType.ARRAY_BUFFER },
        (err, data) => {
          try {
            if (err) {
              reject(err);
              return;
            }
            if (!data || data.responseCode !== 200 || !data.result) {
              reject(new Error(`download failed, responseCode=${data?.responseCode}`));
              return;
            }
            const tmpPath = `${targetPath}.tmp_${Date.now()}`;
            const file = fileIo.openSync(tmpPath,
              fileIo.OpenMode.CREATE | fileIo.OpenMode.WRITE_ONLY | fileIo.OpenMode.TRUNC);
            try {
              fileIo.writeSync(file.fd, data.result as ArrayBuffer);
            } finally {
              fileIo.closeSync(file);
            }
            try {
              fileIo.renameSync(tmpPath, targetPath);
            } catch (renameErr) {
              // 目标已存在（并发预加载）视为成功
              if (fileIo.accessSync(targetPath)) {
                if (fileIo.accessSync(tmpPath)) {
                  fileIo.unlinkSync(tmpPath);
                }
              } else {
                throw renameErr as Error;
              }
            }
            resolve();
          } catch (catchErr) {
            reject(catchErr as Error);
          } finally {
            request.destroy();
          }
        });
    });
  }

  /**
   * 统一将 http url / 本地 uri 化为沙盒文件路径。若是网络地址则下载到缓存。
   */
  private prepareLocalFile(sourceKey: string, subDir: string, extension: string): Promise<string> {
    if (sourceKey && sourceKey.startsWith('http')) {
      const cachePath = this.buildCachePathWithExt(subDir, sourceKey, extension);
      if (fileIo.accessSync(cachePath)) {
        return Promise.resolve(cachePath);
      }
      return this.downloadToFile(sourceKey, cachePath).then(() => cachePath);
    }
    if (sourceKey && sourceKey.startsWith('file://')) {
      return Promise.resolve(sourceKey.substring('file://'.length));
    }
    return Promise.resolve(sourceKey);
  }

  private buildCachePathWithExt(subDir: string, url: string, extension: string): string {
    const dirPath = `${this.context.cacheDir}/kmm_res/${subDir}`;
    if (!fileIo.accessSync(dirPath)) {
      try {
        fileIo.mkdirSync(dirPath, true);
      } catch (err) {
        hilog.error(DOMAIN, TAG, 'mkdir failed, dir=%{public}s, err=%{public}s',
          dirPath, JSON.stringify(err) ?? '');
      }
    }
    return `${dirPath}/${this.fnv1aHash(url)}.${extension}`;
  }

  /**
   * 将沙盒中的媒体文件写入系统相册。
   */
  private async saveMediaToAlbum(localPath: string, mediaType: 'image' | 'video', extension: string): Promise<void> {
    if (!fileIo.accessSync(localPath)) {
      throw new Error(`media file not exist: ${localPath}`);
    }
    const helper = photoAccessHelper.getPhotoAccessHelper(this.context);
    const photoType = mediaType === 'image'
      ? photoAccessHelper.PhotoType.IMAGE
      : photoAccessHelper.PhotoType.VIDEO;
    const assetUri = await helper.createAsset(photoType, extension);
    const dstFile = await fileIo.open(assetUri, fileIo.OpenMode.WRITE_ONLY);
    const srcFile = await fileIo.open(localPath, fileIo.OpenMode.READ_ONLY);
    try {
      await fileIo.copyFile(srcFile.fd, dstFile.fd);
    } finally {
      await fileIo.close(srcFile);
      await fileIo.close(dstFile);
    }
  }

  /**
   * 从本地图片文件中挑选主色，返回 #RRGGBB 十六进制颜色字符串。
   */
  private async pickMainColorFromFile(localPath: string, paletteParam: PaletteParam | null): Promise<string> {
    const source = image.createImageSource(localPath);
    if (!source) {
      return DEFAULT_COLOR_HEX;
    }
    let pixelMap: image.PixelMap | null = null;
    try {
      const targetSize = paletteParam?.imageSize && paletteParam.imageSize > 0 ? paletteParam.imageSize : 330;
      pixelMap = await source.createPixelMap({
        desiredPixelFormat: image.PixelMapFormat.RGBA_8888,
        desiredSize: { width: targetSize, height: targetSize },
      });
      const picker = await effectKit.createColorPicker(pixelMap);
      const mainColor = picker.getMainColorSync();
      return this.toHexColor(mainColor);
    } finally {
      if (pixelMap) {
        pixelMap.release();
      }
      source.release();
    }
  }

  private toHexColor(color: effectKit.Color): string {
    const r = Math.max(0, Math.min(255, Math.round(color.red)));
    const g = Math.max(0, Math.min(255, Math.round(color.green)));
    const b = Math.max(0, Math.min(255, Math.round(color.blue)));
    const hex = ((r << 16) | (g << 8) | b).toString(16).padStart(6, '0');
    return `#${hex.toUpperCase()}`;
  }

  // endregion
}

interface PaletteParam {
  imageSize: number;
  colorCount: number;
  imageRect: string;
}
