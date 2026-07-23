/***
*    !!!  GEN CODE DO NOT EDIT  !!!
***/
export interface OhosAppRouter {
            
   toScheme(scheme: string): void;

   toComposePage(pageName: string, pageData: object, launchType: string): void;

   goBack(): void;

   moveTaskToBack(): void;

}

export interface OhosAppShare {
            
   isShareChannelSupported(channelId: string): boolean;

   shareLogToWeChat(): void;

   shareLogToWeCom(): void;

   shareToChannel(channelId: string, title: string, desc: string, url: string, imagePath: string): void;

}

