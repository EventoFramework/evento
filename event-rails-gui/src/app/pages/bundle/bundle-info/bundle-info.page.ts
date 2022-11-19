import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {BundleService} from "../../../services/bundle.service";
import {bundle} from "@angular-devkit/build-angular/src/builders/browser-esbuild/esbuild";

@Component({
  selector: 'app-bundle-info',
  templateUrl: './bundle-info.page.html',
  styleUrls: ['./bundle-info.page.scss'],
})
export class BundleInfoPage implements OnInit {
  bundleName: string;
  bundle;
  componentHandlers: {}
  components: string[] = [];
  environmentKeys: string[] = [];
  vmOptionsKeys: string[] = [];

  constructor(private route: ActivatedRoute, private bundleService: BundleService) { }

  async ngOnInit() {
    this.bundleName = this.route.snapshot.params.identifier;
    this.bundle = await this.bundleService.find(this.bundleName);
    this.bundle.handlers.sort((a,b) => (a.componentName + a.handlerType).localeCompare(b.componentName + b.handlerType))
    this.componentHandlers = this.bundle.handlers.reduce((c, h) => {
      if(!c[h.componentName]){
        c[h.componentName] = []
      }
      c[h.componentName].push(h);
      return c
    }, {})
    this.components = Object.keys(this.componentHandlers);
    this.environmentKeys = Object.keys(this.bundle.environment);
    this.vmOptionsKeys = Object.keys(this.bundle.vmOptions);
  }

  putEnv(key, value) {
    this.bundleService.putEnv(this.bundleName, key, value).finally();
    this.bundle.environment[key] = value;
    this.environmentKeys = Object.keys(this.bundle.environment);
  }
  removeEnv(key) {
    this.bundleService.removeEnv(this.bundleName, key).finally();
    delete this.bundle.environment[key];
    this.environmentKeys = Object.keys(this.bundle.environment);
  }
  putVmOption(key, value) {
    this.bundleService.putVmOption(this.bundleName, key, value).finally();
    this.bundle.vmOptions[key] = value;
    this.vmOptionsKeys = Object.keys(this.bundle.vmOptionsKeys);
  }
  removeVmOption(key) {
    this.bundleService.removeVmOption(this.bundleName, key).finally();
    delete this.bundle.vmOptions[key];
    this.vmOptionsKeys = Object.keys(this.bundle.vmOptionsKeys);
  }
}
