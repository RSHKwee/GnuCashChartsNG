REM Batch file for generating a CHM file.
REM The hhc.exe returns an exit code 1, when the command is successfull....
REM This batch file superseeds the exit code to 0 
"C:\Program Files (x86)\HTML Help Workshop\hhc" %1%
exit 0
