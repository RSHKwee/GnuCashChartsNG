REM Batch file for InnoSetup.
REM The hhc.exe returns an exit code 1, when the command is successfull....
REM This batch file superseeds the exit code to 0 
"C:\Program Files (x86)\Inno Setup 6\ISCC.exe" %1%
exit 0
