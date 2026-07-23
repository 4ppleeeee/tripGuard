import { KRRecord } from '@kuikly/render';

export const COMPOSE_EVENT_ON_PAGE_NEW_INTENT = 'OnPageNewIntent';
export const COMPOSE_PAGE_LAUNCH_TYPE_SINGLE_INSTANCE = 'SINGLE_INSTANCE';

export type PageNewIntentHandler = (pageData: KRRecord) => boolean;

interface VisibleComposePage {
  instanceId: number;
  pageName: string;
  handler: PageNewIntentHandler;
}

export class OhosComposePageRegistry {
  private static visiblePage?: VisibleComposePage;
  private static pageStack: VisibleComposePage[] = [];
  private static nextId: number = 0;

  static nextPageId(): number {
    OhosComposePageRegistry.nextId += 1;
    return OhosComposePageRegistry.nextId;
  }

  static register(instanceId: number, pageName: string | undefined, handler: PageNewIntentHandler): void {
    if (!pageName) {
      return;
    }
    OhosComposePageRegistry.unregister(instanceId);
    OhosComposePageRegistry.pageStack.push({
      instanceId,
      pageName,
      handler,
    });
  }

  static unregister(instanceId: number): void {
    const index = OhosComposePageRegistry.pageStack.findIndex((page) => page.instanceId === instanceId);
    if (index >= 0) {
      OhosComposePageRegistry.pageStack.splice(index, 1);
    }
    OhosComposePageRegistry.clearVisible(instanceId);
  }

  static markVisible(instanceId: number): void {
    const page = OhosComposePageRegistry.findPageByInstanceId(instanceId);
    if (!page) {
      return;
    }
    OhosComposePageRegistry.visiblePage = page;
  }

  static clearVisible(instanceId: number): void {
    if (OhosComposePageRegistry.visiblePage?.instanceId === instanceId) {
      OhosComposePageRegistry.visiblePage = undefined;
    }
  }

  static dispatchToVisible(pageName: string, pageData: KRRecord): boolean {
    const visiblePage = OhosComposePageRegistry.visiblePage;
    if (!visiblePage || visiblePage.pageName !== pageName) {
      return false;
    }
    return visiblePage.handler(pageData);
  }

  static dispatchToLatest(pageName: string, pageData: KRRecord): boolean {
    const page = OhosComposePageRegistry.findLatestPage(pageName);
    if (!page) {
      return false;
    }
    return page.handler(pageData);
  }

  static backStepsToLatest(pageName: string): number | undefined {
    for (let i = OhosComposePageRegistry.pageStack.length - 1; i >= 0; i--) {
      if (OhosComposePageRegistry.pageStack[i].pageName === pageName) {
        return OhosComposePageRegistry.pageStack.length - 1 - i;
      }
    }
    return undefined;
  }

  private static findPageByInstanceId(instanceId: number): VisibleComposePage | undefined {
    return OhosComposePageRegistry.pageStack.find((page) => page.instanceId === instanceId);
  }

  private static findLatestPage(pageName: string): VisibleComposePage | undefined {
    for (let i = OhosComposePageRegistry.pageStack.length - 1; i >= 0; i--) {
      const page = OhosComposePageRegistry.pageStack[i];
      if (page.pageName === pageName) {
        return page;
      }
    }
    return undefined;
  }
}
