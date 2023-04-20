# Ki2

Ki2 is an _app_/_addon_/_plugin_ for Hammerhead Karoo 2 devices. It is a companion app for electronic shifting groupsets produced by a Japanese company.

[![Buy me a coffee](https://img.shields.io/badge/☕-Buy%20me%20a%20coffee-blue)](https://www.paypal.com/donate/?business=N6PWH859NY7W6&no_recurring=1&item_name=Buy+me+a+coffee&currency_code=EUR) [![Validate workflow](https://github.com/valterc/ki2/actions/workflows/android-validate.yml/badge.svg?branch=main)](https://github.com/valterc/ki2/actions/workflows/android-validate.yml) [![Latest Release](https://img.shields.io/github/v/release/valterc/ki2?label=Latest%20Release)](https://github.com/valterc/ki2/releases/latest/)

## Screenshots

![Ki2 main application](media/image_0.png?raw=true "Ki2 main application")
![Ki2 settings](media/image_1.png?raw=true "Ki2 settings")
![Connection to device](media/image_2.png?raw=true "Connection to device")
![Gears information](media/image_3.png?raw=true "Gears information")
![Edit Karoo profile with Ki2 items](media/image_4.png?raw=true "Edit Karoo profile with Ki2 items")
![Ride profile with Ki2 data elements](media/image_5.png?raw=true "Ride profile with Ki2 data elements")
![Ride profile with Ki2 data elements](media/image_6.png?raw=true "Ride profile with Ki2 data elements")
![Overlay in Ride](media/image_7.png?raw=true "Overlay in Ride")

These screenshots have been _slightly_ edited.

## Features

- Pair and connect to electronic shifting groupsets
- Configure actions for Hood buttons
- Change shifting mode
- Setup Karoo ride profiles with data elements such as:
  - Gears in text format
  - Gear size (teeth count) in text format
  - Gear ratio in text format
  - Battery % in text format
  - Shifting mode in text format
  - Ride shift count in text format
  - Gears in graphical format
  - Gear size (teeth count) in graphical format
  - Drivetrain view in graphical format
  - Drivetrain size (teeth count) in graphical format
  - Change shift mode graphical control
- While in a ride, control Karoo from the Hood buttons:
  - Navigate page left/right
  - Pause/Resume ride
  - Mark lap
  - Zoom map
  - Switch to map page
- Overlay with shifting information while in Ride
- Receive notifications when shifting battery is low
- Audio alerts before upcoming synchro shift or when reaching shifting limits
- Self update

## How can I try this app?

1. Download Ki2 APK file from the latest [release page](https://github.com/valterc/ki2/releases/latest/) (or [here](https://github.com/valterc/ki2/releases/tag/0.8-karoo1) for Karoo 1)
2. Sideload Ki2 into Karoo ([Video](https://www.youtube.com/watch?v=qp7H_ZPQEJY))
3. Connect to shifting devices from Ki2 ([Video](https://user-images.githubusercontent.com/1299179/204136334-c8a5a395-c6b5-4d16-a8d5-ff1fa2dd726d.mp4)) and use Ki2 data elements in Karoo profiles ([Video](https://user-images.githubusercontent.com/1299179/204136325-69bcbf7a-b69f-45ed-96c4-3d9a52112089.mp4))

<details>
<summary>Full Instructions</summary>

1. Check and download the _APK_ file on the latest release found on the [release page](https://github.com/valterc/ki2/releases/latest/)
   - **Karoo 1 users:** The latest release is supported by Karoo 2, for Karoo 1 use [this release](https://github.com/valterc/ki2/releases/tag/0.8-karoo1)
2. Install/Update the APK file on your Karoo:
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
        - **For updates:** If you are updating the Ki2 app, you should use the following command variation: `adb.exe install -r "APK-FILE-PATH"` (with the **-r** argument) to avoid having to uninstall Ki2 before installing the app update.
3. The Ki2 app should appear in the app list below Sensors, Settings, etc
4. Open the Ki2 app to pair and configure a wireless shifting connection ([Video](https://user-images.githubusercontent.com/1299179/204136334-c8a5a395-c6b5-4d16-a8d5-ff1fa2dd726d.mp4))
   - Make sure you don't have the shifting system paired with Karoo (if you are on an old Karoo software version that still supports it)
   - Make sure you have less than 14 ANT devices added/paired in Karoo
   - Open Ki2 and press the + button, searching for shifting devices will start
   - On your shifting system, you need to enable connection/pairing mode. This is usually done by holding the function button for a second or so. (The green and red lights in will flash in alternating order - if your version has those.)
   - The shifting device should appear in Ki2 search results list.
5. Add Ki2 data elements to Karoo profiles from the Karoo profile editor, choose the Ki2 elements when modifying a profile ([Video](https://user-images.githubusercontent.com/1299179/204136325-69bcbf7a-b69f-45ed-96c4-3d9a52112089.mp4))

</details>

### Supported Karoo software versions

Karoo software may change with new updates, for best experience please use the latest _tested_ Karoo version with the [latest Ki2 version](https://github.com/valterc/ki2/releases/latest/).

| Version                   | Supported |
|---------------------------|-----------|
| 1.297.1231 (May 19, 2022) | ✅        |
| 1.333.1345 (Oct 06, 2022) | ✅        |
| _all versions in between_ | ✅        |
| 1.374.1480 (Apr 06, 2023) | ✅        |
| 1.380.1494 (Apr 20, 2023) | ✅        |

## Known issues

### Ki2 data elements don't show up in the ride profile (when Karoo boots)

Unfortunately this is a problem with the Karoo software itself. I've tried different ways to solve this issue but nothing works so far.

**Workaround:** Open the ride app with a profile with any Ki2 data elements, before starting the ride, back out from the ride app and return to the dashboard via the back button. The next time the ride app is opened from the dashboard or from a route, all Ki2 data elements should appear. This workaround is applied automatically if the "Pre load ride application" setting is enabled.

### Hood buttons don't work in the control center or outside the ride app

This is a limitation of the Karoo SDK and a restriction of Android. Unfortunately the Karoo SDK does not provide any way to send commands, so the implementation has to perform restricted Android operations that are not allowed when Ki2 app is on the background.

**Workaround:** There is no known workaround, hood buttons only work in the ride app.

## How does it work?

This app utilizes the Android environment along with the ANT service and the Karoo SDK. It contains 3 main components:

- Device management + settings activity
- Background service that integrates with ANT and communicates with the shifting device wirelessly
- Integration with Karoo via Karoo SDK

## Limitations

Karoo SDK is very limited, here are some things that are not possible:

- Include shifting information in the FIT file
- Control lights from the hood buttons/switches

## Battery usage

While the implementation is fairly tidy and optimized, this app will be yet another process running on the Karoo. This means that there is _some_ battery impact. I did some non-scientific testing.

| Setup                                             |
|---------------------------------------------------|
| Karoo 2 (Early 2021) Software Version: 1.297.1231 |
| Garmin HR                                         |
| Varia Radar + Light                               |
| Built-in GPS                                      |
| Audio alerts on (navigation + radar)              |

| Setup         | Distance | Ride Duration | Recorded Ascent | Avg Speed | Avg Temperature | Total Shifts | Battery Usage                                           |
|---------------|----------|---------------|-----------------|-----------|-----------------|--------------|---------------------------------------------------------|
| Karoo 2       | 52.6km   | 2:06:03       | 592m            | 25.1km/h  | 25C             | 410          | **18%**<br> **8.5% / hour**<br> (Start: 98% - End: 80%) |
| Karoo 2 + Ki2 | 52.6km   | 1:58:21       | 585m            | 26.7km/h  | 21C             | 368          | **15%**<br> **7.5% / hour**<br> (Start: 97% - End: 82%) |

Tested in the same route in different days. Similar profile in Karoo with the original shifting data elements and then the equivalent ones from Ki2.

Battery gain/loss is marginal and negligible for this particular test given the differences in temperature, ride duration, radar alerts and the number of shifts. It is encouraging that the battery consumption did not _noticeable_ increase, that was what I looking for 🙂

## How can I help?

Is this useful for you? [Buy me a coffee](https://www.paypal.com/donate/?business=N6PWH859NY7W6&no_recurring=1&item_name=Buy+me+a+coffee&currency_code=EUR).

**Do you have experience with Android development?** Review the code, point out any problems and feel free to open issues or submit PRs with improvements. Submissions that are outside the _scope_ of the project may be rejected.

**Do you work for Hammerhead?** Love my Karoo 2, thanks for making it awesome! Don't get involved with this project directly, however please support and improve the Karoo SDK. If you want feedback/ideas feel free to reach out.

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

Yes, this is by design. There is a particular file missing from this repo and unfortunately I cannot add it here.

### The UI looks similar to other Karoo applications, do you work for Hammerhead or have access to their code?

No! I don't work for hammerhead and I don't have access to their code. I'm just a programmer with a dusty experience in Android development and an eye for UI. I tried to replicate the Karoo interface (look and feel, colors, etc.) to make the app fit in. You can find the [Karoo SDK with design guidelines here](https://github.com/hammerheadnav/karoo-sdk).

### Hood buttons are inverted or don't work as expected?

It might be because your shifting configuration has a different action assigned to the hood buttons. Ki2 expects _Channel 1_ for left button and _Channel 2_ for right button. If your shifting configuration is different, you might experience weird behavior. Please double check the configuration via the manufacturer official shifting app available on smartphones. _Channel 3_ and _Channel 4_ are currently not supported.

### The app disconnects from the shifting unit while riding

Make sure you are using the [latest Ki2 version](https://github.com/valterc/ki2/releases/latest/). It could be a signal issue, verify the signal between the wireless unit and Karoo. To do that, tap on the shifting device on the list of devices in Ki2. The signal value is displayed on the top when the shifting unit is connected. Signal values between -30 to -60dBm offer best and stable connectivity while a signal in range of -75 to -90dBm is degraded and can cause connectivity issues. There is nothing that can be done in the app/software to improve the signal. Recommendation is to move or rotate the shifting wireless unit, perhaps moving it a small amount could be enough or in some cases it may require relocating the wireless unit to another location in the bicycle closer to Karoo.

### Gear size (teeth count) or gear ratio unknown or not correct

The gear size (teeth count) is automatically obtained from the shifting unit, when supported. If the shifting unit is misconfigured then the wrong information will be displayed in Ki2. Please use the official mobile application from the shifting unit manufacturer to configure the correct chainring information. This might not be supported in all bicycles or shifting systems. It is also possible to use custom gearing information, open the device details in Ki2 to edit custom gearing.

### It does not work or I have a problem

Please open an issue in this github repository. Explain the problem in detail and explain what you are trying to achieve. Keep in mind that there is no official support for this project.

## License and notices

Please review the LICENSE.txt and NOTICES.txt files.
