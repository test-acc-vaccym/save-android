# Save
An Android app for saving links to articles etc. on the go so you can read them later.

## Features
- Save from another app  
  You can select this app from another app's share screen to save links directly to this app.  
  This will also allow you to edit the link and its annotation before saving it.
  Save will also try to separate text from the URL, when an app shares a combination of both.
- Use local storage or API  
  Choose between using the [API](https://github.com/albalitz/save-api) so you can access your links on other devices, or keep them offline on your phone.
- Choose the sort order  
  Either oldest first (default) or newest first.
- Import/Export  
  Save and restore your saved links to/from a json file on your phone's external storage.  
  Currently /sdcard/Documents/Save/save-link-export.json
- Share links to other apps  
  send your links to another app to share with friends, or save in another app
- Offline queue when saving with an api fails  
  Your links will be queued and you can send them to the api again later.

## Screenshots
| **Dialog for saving a link** | **Saved link and confirmation** |
| :--------------------------: | :-----------------------------: |
| ![add_dialog](screenshots/add_dialog.jpg) | ![saved_confirmation](screenshots/link_saved.jpg) |
| **Dialog overlay when saving a link from another app** | |
| ![share_overlay](screenshots/share_overlay.jpg) | |
