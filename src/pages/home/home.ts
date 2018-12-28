import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
import { CallNumber } from '@ionic-native/call-number';
import { Platform } from 'ionic-angular';
import { Subscription } from 'rxjs';
import { Observable } from 'rxjs/Observable.js'
import 'rxjs/add/observable/interval';

import backgroundVideo from '../../../plugins/cordova-plugin-background-video/www/backgroundVideo';

@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})
export class HomePage {
  public count: number = 0;
  private onTimerSubscription: Subscription;
  private onResumeSubscription: Subscription;

  constructor(
    public navCtrl: NavController,
    private callNumber: CallNumber,
    platform: Platform) {
    this.onResumeSubscription = platform.resume.subscribe(() => {
      // do something meaningful when the app is put in the foreground
      console.error("RESUME APP");
    });

  }

  public startTimer() {
    this.count = 0;
    this.onTimerSubscription = Observable.interval(1000).subscribe(x => {
      this.count++;
    })
  }

  public startPhoneCall() {
    //this.callNumber.callNumber("0664545968", true);
    backgroundVideo.coolMethod("it works great!!!", (res) => console.debug(res), (res) => console.error(res));
  }

  public stopTimer() {
    this.onTimerSubscription.unsubscribe();
  }

  ngOnDestroy() {
    // always unsubscribe your subscriptions to prevent leaks
    this.onTimerSubscription.unsubscribe();
    this.onResumeSubscription.unsubscribe();
  }

}
