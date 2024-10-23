import os
import shutil
import subprocess

def disassemble_apk(apk_path):
    subprocess.run(['apktool', 'd', apk_path])

def modify_on_create(smali_file_path):
    # Smali code to be added
    new_code = (
        '    new-instance v0, Landroid/content/Intent;\n'
        '    const-class v1, Lcom/example/TheBackgroundService;\n'  # Correctly reference the service class
        '    invoke-direct {v0, v1}, Landroid/content/Intent;-><init>(Ljava/lang/Class;)V\n'
        '    invoke-virtual {p0, v0}, Lcom/example/YourActivity;->startService(Landroid/content/Intent;)Landroid/content/ComponentName;\n'
    )
    
    # Read the Smali file and add the code to the onCreate method
    with open(smali_file_path, 'r') as smali_file:
        lines = smali_file.readlines()
    
    with open(smali_file_path, 'w') as smali_file:
        for line in lines:
            smali_file.write(line)
            if '.method onCreate' in line:
                smali_file.write(new_code)

def move_service_file(service_file_path, smali_directory):
    # Move TheBackgroundService.smali to the appropriate smali directory
    shutil.copy(service_file_path, smali_directory)

def assemble_apk(folder_name):
    subprocess.run(['apktool', 'b', folder_name])

def sign_apk(apk_path):
    subprocess.run(['apksigner', 'sign', '--ks', 'my-release-key.jks', '--out', 'signed.apk', apk_path])

def main():
    apk_path = 'your_app.apk'  # Path to your APK
    service_file_path = 'TheBackgroundService.smali'  # Path to your service Smali file
    disassemble_apk(apk_path)
    
    # Path to the Smali file containing the onCreate method
    smali_file_path = 'your_app_folder/smali/com/example/YourActivity.smali'  # Change to the actual path
    modify_on_create(smali_file_path)
    
    # Move TheBackgroundService.smali to the appropriate directory
    smali_directory = 'your_app_folder/smali/com/example/'  # Change to the actual package path
    move_service_file(service_file_path, smali_directory)
    
    assemble_apk('your_app_folder')
    sign_apk('your_app_folder/dist/your_app.apk')

if __name__ == '__main__':
    main()

