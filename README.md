# gplay-batchtool
On the current version of Google Play it is no longer possible to batch-remove all entries of uninstalled apps from the 'Library' tab.

This app uses the accessibility service to automate manually clicking on each entry and removing it from that list.

When all requirements are met, the service will display a control bar on the screen. This control bar has three buttons:

* Exit: To quit the service
* Stop: To cancel the automation
* Play: To start the automation

The will perform the following actions:
1. Open Google Play
2. Navigate to the home page
3. Open the navigation drawer
4. Click on `My Apps & Games`
5. Click on `Library`
6. Click the `X` on the first app item
7. Confirm the removal and go to step 6 again while there are more `X` to be clicked.