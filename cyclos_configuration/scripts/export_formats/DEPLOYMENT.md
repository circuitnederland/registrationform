# Add MT940 and CAMT.053 transaction export formats

Note: the MT940 export used to be a Custom operation. This can now be replaced with a Cyclos 'Export format'.

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
