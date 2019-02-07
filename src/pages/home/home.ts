import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
import { CallNumber } from '@ionic-native/call-number';
import { Platform } from 'ionic-angular';
import { Subscription } from 'rxjs';
import 'rxjs/add/observable/interval';

import backgroundVideo from '../../../plugins/cordova-plugin-background-video/www/backgroundVideo';

declare var cordova: any
@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})
export class HomePage {
  private onResumeSubscription: Subscription;

  constructor(
    public navCtrl: NavController,
    private callNumber: CallNumber,
    platform: Platform) {
    this.onResumeSubscription = platform.resume.subscribe(() => {
      // do something meaningful when the app is put in the foreground
      console.log("APP is back in FOREGROUND");
    });

  }

  public hasCamera() {
    backgroundVideo.hasCamera(
      (suc) => { console.log(suc) },
      (err) => { console.error("error code : " + err) }
    );
  }

  public startVideoRecord() {
    //set file destination 
    let fileDest: String = null;

    // use of 'externalRootDirectory' for android only: 
    fileDest = cordova.file.externalRootDirectory + "Pictures/KipKare/myVideo.mp4";

    fileDest = fileDest.replace("file://", "");
    backgroundVideo.startVideoRecord(
      fileDest,
      (suc) => { console.log(suc) },
      (err) => { console.error("error code : " + err) }
    );
  }

  public startPhoneCall() {
    this.callNumber.callNumber("0664545968", true);
  }

  public stopVideoRecord() {
    backgroundVideo.stopVideoRecord(
      (suc) => { console.log(suc) },
      (err) => { console.error("error code : " + err) }
    );
  }

  ngOnDestroy() {
    // always unsubscribe your subscriptions to prevent leaks
    this.onResumeSubscription.unsubscribe();
  }

}
