# Ki2

Ki2 is an extension for Hammerhead Karoo cycling computers. It is a companion app for electronic shifting groupsets produced by a Japanese company.

[![Buy me a coffee](https://img.shields.io/badge/☕-Buy%20me%20a%20coffee-blue)](https://www.paypal.com/donate/?business=N6PWH859NY7W6&no_recurring=1&item_name=Buy+me+a+coffee&currency_code=EUR) [![Validate workflow](https://github.com/valterc/ki2/actions/workflows/android-validate.yml/badge.svg?branch=main)](https://github.com/valterc/ki2/actions/workflows/android-validate.yml) [![Latest Release](https://img.shields.io/github/v/release/valterc/ki2?label=Latest%20Release)](https://github.com/valterc/ki2/releases/latest/)

## Screenshots

![Ki2 main application](media/image_1.png?raw=true "Ki2 main application")
![Device information](media/image_2.png?raw=true "Device information")
![Device information](media/image_3.png?raw=true "Device information")
![Ki2 settings](media/image_4.png?raw=true "Ki2 settings")
![Ride elements](media/image_5.png?raw=true "Ride elements")
![Overlay](media/overlay.gif?raw=true "Overlay")

## Features

- Pair and connect to electronic shifting groupsets
- Integrate shifting information with native Karoo elements
- Control Karoo screens and trigger actions from the shifting buttons
- Change shifting mode
- Overlay shifting information on top of the ride display
- Receive notifications when shifting battery is low
- Audio alerts before upcoming synchro shift or when reaching shifting limits

> [!NOTE]
> The following features require the Ki2 shifting devices to be added to Karoo as extension sensors:
>
> - Use of native Karoo ride elements
> - FIT file recording of gear shifts
> - Low battery notifications

## How can I install the app?

1. Find the link to the Ki2 APK file from the latest [release page](https://github.com/valterc/ki2/releases/latest/)
2. [Sideload Ki2 into Karoo](https://support.hammerhead.io/hc/en-us/articles/31576497036827-Companion-App-Sideloading) (For Karoo 2 follow this [video](https://www.youtube.com/watch?v=qp7H_ZPQEJY))
3. [Connect to shifting devices from Ki2](https://user-images.githubusercontent.com/1299179/204136334-c8a5a395-c6b5-4d16-a8d5-ff1fa2dd726d.mp4)
   - Open Ki2
   - Press the `+` button
   - Make sure a wireless shifting device is nearby and that it is active (perform a shift or press a button to wake up the shifting system)
   - Press the `Add` button in Ki2 to add a device once it is found
4. Configure the shifting device from Karoo (for example: rename the device - optional)
5. Add the Ki2 devices to Karoo (important for all features to work)
   - Open Karoo sensors
   - Press the `+` button to add a new sensor
   - Choose the `Extensions` sensor type (the icon with the puzzle piece)
   - Select all shifting connections that were previously added in Ki2 (no scan for new devices is possible here, you must add the device first in Ki2)
   - Configure gearing information in Karoo sensors
6. Use default Karoo shifting elements or Ki2 elements in your ride profile
7. Customize battery alerts, shifting audio alerts, button controls, overlays and other features from Ki2

> [!NOTE]
> It is important that the gearing configuration is correctly set in Ki2, and that the order of shifting sensors in the Karoo sensors list matches with the order of shifting devices in Ki2. The first shifting device (from the top of the list) that is connected, will be used as the main source for the shifting data.

### Supported Karoo software versions

For best experience please use the latest Karoo software and the [latest Ki2 version](https://github.com/valterc/ki2/releases/latest/). Please report any issues if you experience problems after updating your Karoo.

Ki2 requires Karoo version `1.527.2014` or later.

## Battery usage

While the implementation is fairly tidy and optimized, this app will be yet another process running on the Karoo. This means that there is _some_ battery impact. I did some non-scientific testing.

| Setup                                             |
|---------------------------------------------------|
| Karoo 2 (Early 2021) Software Version: 1.297.1231 |
| Garmin HR                                         |
| Varia Radar + Light                               |
| Built-in GPS                                      |
| Audio alerts on (navigation + radar)              |

| Setup         | Distance | Ride Duration | Avg Temperature | Total Shifts | Battery Usage                                           |
|---------------|----------|---------------|-----------------|--------------|---------------------------------------------------------|
| Karoo 2       | 52.6km   | 2:06:03       | 25C             | 410          | **18%**<br> **8.5% / hour**<br> (Start: 98% - End: 80%) |
| Karoo 2 + Ki2 | 52.6km   | 1:58:21       | 21C             | 368          | **15%**<br> **7.5% / hour**<br> (Start: 97% - End: 82%) |

Tested in the same route in different days. Similar profile in Karoo with the original shifting data elements and then the equivalent ones from Ki2.

Battery gain/loss is marginal and negligible for this particular test given the differences in temperature, ride duration, radar alerts and the number of shifts. It is encouraging that the battery consumption did not _noticeably_ increase.

## How can I help?

Is this useful for you? [Buy me a coffee](https://www.paypal.com/donate/?business=N6PWH859NY7W6&no_recurring=1&item_name=Buy+me+a+coffee&currency_code=EUR).

## Support + Future development

This project is actively maintained but without any guarantees. It might stop working or lose functionality in newer Karoo software versions.

Features might be added but there is no development plan.

## FAQ

### Can you add a new feature?

Maybe, it might or might not be possible, given that:

- Karoo extensions have a limited set of features.
- The information from the shifting system may not be well understood, since many details are not publicly available.

Open an issue and I might consider working on it, or feel free to contribute yourself. Submissions that are outside the _scope_ of the project may be rejected.

### I tried cloning the repo but the build fails on my machine

Yes, this is by design. There is a particular file missing from this repo and unfortunately I cannot add it here.

### How to configure hood buttons/switches?

Typically _Channel 1_ is used for the left hood button and _Channel 2_ for the right hood button. Set the channels via the manufacturer official shifting app available on smartphones. You can use the device details in Ki2, by pressing on a device name, to verify the channel actions.

### The app disconnects from the shifting unit while riding

Make sure you are using the [latest Ki2 version](https://github.com/valterc/ki2/releases/latest/). It could be a signal issue, verify the signal between the wireless unit and Karoo. To do that, tap on the shifting device on the list of devices in Ki2. The signal value is displayed on the top when the shifting unit is connected. Signal values between -30 to -60dBm offer best and stable connectivity while a signal in range of -75 to -90dBm is degraded and can cause connectivity issues. There is nothing that can be done in the app/software to improve the signal. Recommendation is to move or rotate the shifting wireless unit, perhaps moving it a small amount could be enough or in some cases it may require relocating the wireless unit to another location in the bicycle closer to Karoo.

### Gear size (teeth count) or gear ratio unknown or not correct

The gear size (teeth count) is automatically obtained from the shifting unit, when supported. If the shifting unit is misconfigured then the wrong information will be displayed in Ki2. Please use the official mobile application from the shifting unit manufacturer to configure the correct chainring information. This might not be supported in all bicycles or shifting systems. It is also possible to use custom gearing information, open the device details in Ki2 to edit custom gearing. You should also configure the Karoo sensor with the same gearing information - this has to be done manually.

### It does not work or I have a problem

If after you diagnose/troubleshoot the problem you still cannot find a solution, please open an issue in this github repository. Explain the problem in detail and explain what you are trying to achieve. Keep in mind that there is no official support for this project.

## License and notices

Please review the LICENSE.txt and NOTICES.txt files.
