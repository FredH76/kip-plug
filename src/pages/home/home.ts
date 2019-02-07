import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
import { CallNumber } from '@ionic-native/call-number';
import { Platform } from 'ionic-angular';
import { Subscription } from 'rxjs';
import 'rxjs/add/observable/interval';

import backgroundVideo from '../../../plugins/cordova-plugin-background-video/www/backgroundVideo';
//import deviceInfo from '../../../plugins/cordova-plugin-deviceinformation/www/deviceinformation';

declare var cordova: any
@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})
export class HomePage {
  private onResumeSubscription: Subscription;
  private myPhonNumber: String;
  private quality: number = 1;

  constructor(
    public navCtrl: NavController,
    private callNumber: CallNumber,
    platform: Platform) {
    this.onResumeSubscription = platform.resume.subscribe(() => {
      // do something meaningful when the app is put in the foreground
      console.log("APP is back in FOREGROUND");

      /*deviceInfo.get((res) => {
        this.myPhonNumber = res
      });*/

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
    let dateNow = new Date();
    let timeStamp: String = (dateNow.getMonth() + 1) + "_"
      + dateNow.getDate() + "_"
      + dateNow.getHours() + "_"
      + dateNow.getMinutes() + "_"
      + dateNow.getSeconds();
    fileDest = cordova.file.externalRootDirectory + "Pictures/KipKare/kip_" + timeStamp + ".mp4";

    backgroundVideo.setQuality(this.quality);

    backgroundVideo.startVideoRecord(
      fileDest,
      (suc) => { console.log(suc) },
      (err) => { console.error("error code : " + err) }
    );
  }

  public startPhoneCall() {
    this.callNumber.callNumber("0612345678", true);
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
