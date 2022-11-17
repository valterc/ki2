# Ki2

Ki2 is an _app_/_addon_/_plugin_ for Hammerhead Karoo 2 devices. It is a companion app for electronic shifting groupsets produced by a Japanese company.

## Screenshots

![Ki2 main application](images/image_0.png?raw=true "Ki2 main application")
![Ki2 settings](images/image_1.png?raw=true "Ki2 settings")
![Connection to device](images/image_2.png?raw=true "Connection to device")
![Gears information](images/image_3.png?raw=true "Gears information")
![Edit Karoo profile with Ki2 items](images/image_4.png?raw=true "Edit Karoo profile with Ki2 items")
![Ride profile with Ki2 data elements](images/image_5.png?raw=true "Ride profile with Ki2 data elements")
![Ride profile with Ki2 data elements](images/image_6.png?raw=true "Ride profile with Ki2 data elements")

These screenshots have been _slightly_ edited.

## Features

- Pair and connect to electronic shifting groupsets
- Configure actions for Hood buttons
- Change shifting mode
- Setup Karoo ride profiles with data elements such as:
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
- Receive notifications when shifting battery is low

## How does it work?

This app utilizes the Android environment along with the ANT service and the Karoo SDK. It contains 3 main components:

- Device management + settings activity
- Background service that integrates with ANT and communicates with the shifting device wirelessly
- Integration with Karoo profiles via the Karoo SDK

## How can I try this app?

1. Check and download the _APK_ file on the latest release found on the right side of the GitHub page
2. Install the APK file on your Karoo:
    - (Recommended) [Follow this guide from DC Rainmaker](https://www.dcrainmaker.com/2021/02/how-to-sideload-android-apps-on-your-hammerhead-karoo-1-karoo-2.html)
   1. Activate Karoo developer options:
      - Open Settings > About
      - Tap the Build Number several times until a message appears saying that "You are now a Developer!"
   2. Go to Settings > Developer Options
      - Make sure Developer Options is ON
      - Enable USB Debugging
   3. Download [Android Developer Tools](https://developer.android.com/studio/releases/platform-tools), extract to a directory after download
   4. Open a Terminal or Command Prompt in the directory of the Android tools
   5. Execute `adb.exe devices` (`.\adb.exe devices`)
   6. If a prompt appears on the Karoo to authorize the request, select _Always allow from this computer_ and press _accept_
   7. The output from the adb command should show your Karoo device
   8. Install the APK with the following command: `adb.exe install "APK-FILE-PATH"` (`.\adb.exe install "APK-FILE-PATH"`)
        - Make sure to replace the `APK-FILE-PATH` with the disk location of the APK file that was downloaded
3. The Ki2 app should appear in the app list below Sensors, Settings, etc
4. Open the Ki2 app to pair and configure a wireless shifting connection
5. Add Ki2 data elements to Karoo profiles from the Karoo profile editor, choose the Ki2 elements when modifying a profile

### Supported Karoo software versions

Karoo software may change with new updates, for best experience please use the latest Ki2 version or the version tested with your Karoo software version.

| Version                   | Supported | Tested Ki2 Version |
| -----                     | -----     | -----              |
| 1.297.1231 (May 19, 2022) | ✅        | 0.4                |
| 1.333.1345 (Oct 06, 2022) | ✅        | 0.5                |
| 1.335.1353 (Oct 20, 2022) | ✅        | 0.5                |
| 1.342.1374 (Nov 17, 2022) | ✅        | 0.6                |

## Known issues

### Ki2 data elements don't show up in the ride profile (when Karoo boots)

Unfortunately this is a problem with the Karoo software itself. I've tried different ways to solve this issue but nothing works so far.

**Workaround:** Open the ride app with a profile with any Ki2 data elements, before starting the ride, back out from the ride app and return to the dashboard via the back button. The next time the ride app is opened from the dashboard or from a route, all Ki2 data elements should appear. This workaround is applied automatically if the "Pre load ride application" setting is enabled.

### Hood buttons don't work in the control center or outside the ride app (or in a ride profile without any Ki2 data elements)

This is a limitation of the Karoo SDK and a restriction of Android. Unfortunately the Karoo SDK does not provide any way to send commands, so the implementation has to perform restricted Android operations that are not allowed when Ki2 app is on the background.

**Workaround:** There is no known workaround, hood buttons only work in the ride app when a Ki2 data element is present on any page.

## Battery usage

While the implementation is fairly tidy and optimized, this app will be yet another process running on the Karoo. This means that there is _some_ battery impact. I did some non-scientific testing.

| Setup                                                |
| -----                                                |
| Karoo 2 (Early 2021) Software Version: 1.297.1231    |
| Garmin HR                                            |
| Varia Radar + Light                                  |
| Built-in GPS                                         |
| Audio alerts on (navigation + radar)                 |

| Setup                         | Distance | Ride Duration | Recorded Ascent | Avg Speed | Avg Temperature | Total Shifts | Battery Usage                                           |
| -----                         | -----    | -----         | -----           | -----     | -----           | -----        | -----                                                   |
| Karoo 2                       | 52.6km   | 2:06:03       | 592m            | 25.1km/h  | 25C             | 410          | **18%**<br> **8.5% / hour**<br> (Start: 98% - End: 80%) |
| Karoo 2 + Ki2                 | 52.6km   | 1:58:21       | 585m            | 26.7km/h  | 21C             | 368          | **15%**<br> **7.5% / hour**<br> (Start: 97% - End: 82%) |

Tested in the same route in different days. Similar profile in Karoo with the original shifting data elements and then the equivalent ones from Ki2.

While it may seem that the battery consumption actually got a little bit better, I would say that battery gain/loss is marginal and negligible for this particular test given the differences in temperature, ride duration, radar alerts and the number of shifts. It is encouraging that the battery consumption did not _vastly_ increase, that was what I looking for :)

## How can I help?

Is this useful for you? [Buy me a coffee](https://www.paypal.com/donate/?business=N6PWH859NY7W6&no_recurring=1&item_name=Buy+me+a+coffee&currency_code=EUR).

**Do you have experience with Android development?** Review the code, point out any problems and feel free to open issues or submit PRs with improvements. Submissions that are outside the _scope_ of the project may be rejected.

**Do you work for Hammerhead?** Love my Karoo 2, thanks for making it awesome! Please don't get involved with this project directly, however please work to improve the Karoo SDK:

- Fix the problem with SDK data elements in the ride app. SDK data elements don't load the first time that the ride app is opened after boot.
- Add support to send custom input Keys from SDK module.
- Allow to control the Numeric data view when for example there is no data available. At the moment it is only possible to set the Text, not the size, style, color, etc.
- Add support for more SDK data views, for example add smaller graphical data views.
- Add support for reading/writing data streams, for example to provide shifting data to existing Karoo data elements. Maybe add virtual/SDK sensors.

**Do you do UI/art?** Feel free to create/suggest a new icon for the app.

Leave comments and suggestions.

## Support + Future development

This project is actively maintained but without any guarantees. It might stop working or lose functionality in newer Karoo software versions.

Features might be added but there is no development plan.

## FAQ

### This does not do __!?

While I tried to replicate the original Karoo feature set, there are several limitations in the Karoo SDK and the way Android development works. Not every feature could be faithfully replicated.

### Can you add feature __?

Maybe, it might or might not be possible. As previously stated there are plenty of restrictions with Karoo SDK. Open an issue and I might consider but without any promises.

### I tried cloning the repo but the build fails on my machine

Yes, this is by design. There is a particular file missing from this repo and unfortunately I cannot add it here. It is however available on public locations or through official ways.

### I thought it used a proprietary ANT profile

All information is publicly available on the internet.

### The UI looks similar to other Karoo applications, do you work for Hammerhead or have access to their code?

No! I don't work for hammerhead and I don't have access to their code. I'm just a programmer with a dusty experience in Android development and an eye for UI. I tried to replicate the Karoo interface (look and feel, colors, etc.) to make the app fit in. You can find the [Karoo SDK with design guidelines here](https://github.com/hammerheadnav/karoo-sdk).

### Hood buttons are inverted or don't work as expected?

It might be because your shifting configuration has a different action assigned to the hood buttons. Ki2 expects _Channel 1_ for left button and _Channel 2_ for right button. If your shifting configuration is different, you might experience weird behavior. Please double check the configuration via the manufacturer's official shifting app available on smartphones. _Channel 3_ and _Channel 4_ are currently not supported.

### It does not work or I have a problem

Please open an issue in this github repository. Explain the problem and explain what you are trying to achieve. Keep in mind that there is no official support for this project.

## License and notices

Please review the LICENSE.txt and NOTICES.txt files.
