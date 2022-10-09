# Ki2

_**Notice**: This application is hereby available as a demonstration of technology / proof of concept and cannot in any terms be sold or offered for sale. Please refer to copyright notice._

Ki2 is an _app_/_addon_/_plugin_ for Hammerhead Karoo 2 devices. It is a companion app for electronic shifting groupsets produced by a Japanese company.

## How it works?

This app utilizes the Android environment along with the ANT service and the Karoo SDK. The app contains 3 main components:

- Device management + settings activity
- Background service that integrates with ANT and communicates with the  shifting device wirelessly
- Integration with Karoo profiles via the Karoo SDK

## Features

- Pair and connect to electronic shifting groupsets
- Configure actions for Hood buttons
- Change shifting mode
- Setup Karoo ride profiles with widgets such as:
  - Gears in text format
  - Battery % in text format
  - Shifting mode in text format
  - Ride shift count in text format
  - Gears in graphical format
  - Drivetrain view in graphical format
- While in a ride, control Karoo from the Hood buttons:
  - Navigate page left/right
  - Pause/Resume ride
  - Mark lap
  - Zoom map

## How can I try this app?



## Known issues

### Ki2 widgets don't show up in the ride profile (when Karoo boots)

Unfortunately this is a problem with the Karoo software itself. I've tried different ways to solve this issues but nothing works so far.
**Workaround:** Open the ride app with a profile with any Ki2 widgets,  before starting the ride, back out from the ride app and return to the dashboard via the back button. The second time that the ride app is loaded (from the dashboard or from a route) it should contain all Ki2 widgets.

### Hood buttons don't work when the notification overlay is displayed or outside the ride app (or in the ride app without any Ki2 widgets added)

This is a limitation of the Karoo SDK and a restriction of Android. Unfortunately the Karoo SDK does not provide any ways of performing input, so the implementation has to perform restricted Android operations that are not allowed when Ki2 app in on the background.
**Workaround:** There is no known workaround, hood buttons only work in the ride app when a Ki2 widget is present on any page.

## Battery usage

While the implementation is fairly tidy and optimized, this app will be yet another process running on the Karoo. This means that there is _some_ battery impact. I did some non-scientific testing.

| Setup  |
|-----|
| Karoo 2 (Early 2021) SW: 4.106.1-47e6ad8c63 |
| Garmin HR |
| Varia Radar + Light |
| Audio alerts on (navigation + radar) |

| Setup | Distance | Ride Time | Recorded Ascent | Avg Speed | Avg Temperature | Total Shifts | Battery Usage |
|-----|-----|-----|-----|-----|-----|-----|-----|-----|
| Karoo 2 with original widgets | 52.6km | 2:06:03 | 592m | 25.1kmh | 25C | 410 | **18%**<br> **8.5% / hour**<br> (Start: 98% - End: 80%) |
| Karoo 2 + Ki2 | 52.6km | 1:58:21 | 585m | 26.7kmh | 21C | 368 |  **15%**<br> **7.5% / hour**<br> (Start: 97% - End: 82%) |

Tested in the same route in different days. Similar profile in Karoo with the original shifting widgets and then the equivalent ones from Ki2.

While it may seem that the battery consumption actually got a little bit better, I would say that battery gain/loss is marginal and negligible for this particular test given the differences in temperature, ride time,  radar alerts and the number of shifts. It is encouraging that the battery consumption did not _vastly_ increase, that was what I looking for :)

## How can I help?

Is this useful for you? [Buy me a coffee](https://www.paypal.com/donate/?business=N6PWH859NY7W6&no_recurring=1&item_name=Buy+me+a+coffee&currency_code=EUR).

**Do you have experience with Android development?** Review the code, point out any problems and feel free to open issues or submit PRs with improvements. Submissions that are outside the _scope_ of the project may be rejected.

**Do you work for Hammerhead?** Love my Karoo 2, thanks for making it awesome! Please don't get involved with this project directly, however please work to improve the Karoo SDK:

- Fix the problem with SDK data items in the ride app. SDK data items don't load the first time that the ride app is opened after boot.
- Add support to send custom input Keys from SDK module.
- Allow to control the Numeric data view when for example there is no data available. At the moment it is only possible to set the Text, not the size, style, color, etc.
- Add support for more SDK data views, for example add smaller graphical data views.
- Add support for reading/writing data streams, for example to provide shifting data to existing Karoo widgets. Virtual/SDK sensors?

## Support + Future development

This project is actively maintained but without any guarantees. It might stop working or lose functionality in newer Karoo software versions.

Features might be added but there is no development plan.

## FAQ

**This does not do __!?**
While I tried to replicate the original Karoo feature set, there are several limitations in the Karoo SDK and the way Android development works. Not every feature could be faithfully replicated.

**Can you add feature __?**
Maybe, it might or might not be possible. As previously stated there are plenty of restrictions with Karoo SDK. Open an issue and I might consider but without any promises.

**I tried cloning the repo but the build fails on my machine**
Yes, this is by design. There is a particular file missing from this repo and unfortunately I cannot add it here. It is however available through official ways.

**The UI looks similar to other Karoo applications, do you work for Hammerhead or have access to their code?**
No! I don't work for hammerhead and I don't have access to their code. I'm just a programmer with a dusty experience in Android development and an eye for UI. I tried to replicate the Karoo interface (look and feel, colors, etc.) to make the app fit in.

**This does not work for me or I have a problem**
Please open an issue in this github repository. Explain the problem and what you are trying to achieve. Keep in mind that there is no official support for this project.
