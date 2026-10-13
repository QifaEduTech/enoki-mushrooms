@echo off
chcp 65001 >nul
cd /d "D:\22\Smart-Enokimushroom\python-traceability-demo"
"D:\epam\python1\python.exe" server.py > server.out.log 2> server.err.log
