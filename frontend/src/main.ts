import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

// Standalone-component bootstrap entry point — no NgModule in this app.
bootstrapApplication(AppComponent, appConfig).catch((err) => console.error(err));
