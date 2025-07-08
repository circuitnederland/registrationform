# Add MT940 and CAMT.053 transaction export formats

The MT940 export used to be a Custom operation. This can now be replaced with a Cyclos 'Export format'.

## Scripts

Create a script of type Library:  
Name : exportFormats Library  
Script: the contents of {scripts\export_formats\Library.groovy}

Create a script of type 'Export format':  
Name : Export format CAMT.053  
Included libraries: exportFormats Library  
Parameters: paste the contents of {scripts\export_formats\camt053.properties}  
Script: the contents of {scripts\export_formats\ExportFormat_CAMT053.groovy}

Create a script of type 'Export format':  
Name : Export format MT940  
Included libraries: exportFormats Library  
Parameters: paste the contents of {scripts\export_formats\mt940.properties}  
Script: the contents of {scripts\export_formats\ExportFormat_MT940.groovy}

## Export formats
Under System > [System configuration] 'Export formats' add two new export formats:

Name: MT940  
Internal name: mt940  
Content type: application/octet-stream  
Binary: No  
Character encoding: UTF-8  
File extension: mt940  
Contexts: Account history  
Script: Export format MT940

Name: CAMT.053  
Internal name: camt053  
Content type: application/xml  
Binary: No  
Character encoding: UTF-8  
File extension: xml  
Contexts: Account history  
Script: Export format CAMT.053

## Remove the old Custom operations for MT940

We let the Custom operations remain for a while, but point them to a menu page explaining the changed location.

Go to Content > [Content management] Menu and pages > Default for Nederland > Add a Floating page. Fill in the Label, Title and Content as requested by the business. After saving, take note of the page Id.

Go to System > [Tools] Custom operations. Change both the 'MT940 Handelsrekening' and 'MT940 Kredietrekening':  
Script: Menupagina's als custom operatie  
Script parameters: "pageId = xxxx" (instead of xxxx fill in the page id of the floating menu page created earlier)  
Result type: Rich text  
Information text: {make empty}  
Save and then remove the 'Periode' Form field and the Custom operation on the Actions tab.

Go to System > [Tools] Custom operations. Remove the 'Selecteer periode (MT940)' internal operation.

Go to System > [Tools] Scripts. Remove the scripts 'MT940 export' (Custom operation), 'MT940 genereer periodes' (Load custom field values) and 'MT940 selecteer periode' (Custom operation).