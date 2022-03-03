# Hidden Operations in Mobile AR

A collaborative research effort between Temple University and Stony Brook University



**Faculty PI's**:

- Chiu C. Tan, PhD - *Temple University*

- Haibin Ling, PhD - *Stony Brook University*

**Graduate Students**:

- Sarah M. Lehman - *PhD Student, Temple University*
- Abrar S. Alrumayh - *PhD Student, Temple University*
- Kunal Kolhe - *Masters Student, Stony Brook University*



This code base is centered around three Android Studio applications:

- **mlkit** - the original / honest application
- **mlkit_comp** - the complementary malicious application
- **mlkit_orth** - the orthogonal malicious application



Each of these applications can be installed and executed independently on an Android device.  The primary data collection script is **/scripts/run_test_suite.sh**.  The script, when executed, will launch the app and collect resource consumption traces for a preset period of time.  Class-level constants (**PACKAGE** and **APP_NAME**) can be updated to point to the correct app.