To run test program execute next commands:

```shell
git clone https://github.com/alex-k86/tpv-gauss.git
cd tpv-gauss
./mvnw clean compile exec:java      - Linux
./mvnw.cmd clean compile exec:java  - Windows
```

To run JMH benchmarks execute next commands: 
```shell
./mvnw clean verify      - Linux
./mvnw.cmd clean verify  - Windows
java --add-modules=jdk.incubator.vector -jar target/gauss-benchmarks.jar -f 1 -wi 5 -i 5
```

JMH results for LINEAR_SYSTEM_SIZE=1024:
```
Benchmark                        Mode  Cnt  Score   Error  Units
GaussBenchmark.a_sequential      avgt    5  0.174 ± 0.019   s/op
GaussBenchmark.b_vectorApi       avgt    5  0.179 ± 0.018   s/op
GaussBenchmark.c_multithreading  avgt    5  0.182 ± 0.006   s/op
```

JMH results for LINEAR_SYSTEM_SIZE=2048:
```
Benchmark                        Mode  Cnt  Score   Error  Units
GaussBenchmark.a_sequential      avgt    5  2.359 ± 0.030   s/op
GaussBenchmark.b_vectorApi       avgt    5  2.383 ± 0.024   s/op
GaussBenchmark.c_multithreading  avgt    5  1.293 ± 0.016   s/op
```


Test machine `lscpu` output:
```           
Architecture:            x86_64
  CPU op-mode(s):        32-bit, 64-bit
  Address sizes:         43 bits physical, 48 bits virtual
  Byte Order:            Little Endian
CPU(s):                  16
  On-line CPU(s) list:   0-15
Vendor ID:               AuthenticAMD
  Model name:            AMD Ryzen 7 1700 Eight-Core Processor
    CPU family:          23
    Model:               1
    Thread(s) per core:  2
    Core(s) per socket:  8
    Socket(s):           1
    Stepping:            1
    Frequency boost:     disabled
    CPU max MHz:         3500.0000
    CPU min MHz:         1550.0000
    BogoMIPS:            6989.10
    Flags:               fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush mmx fxsr sse sse2 ht syscall nx mmxext fxsr_opt pdpe1gb rdtscp lm constant_tsc rep_good nopl nonstop_tsc cpuid extd_apicid aperfmperf
                         rapl pni pclmulqdq monitor ssse3 fma cx16 sse4_1 sse4_2 movbe popcnt aes xsave avx f16c rdrand lahf_lm cmp_legacy svm extapic cr8_legacy abm sse4a misalignsse 3dnowprefetch osvw skinit wdt tce topoext perfctr_core
                         perfctr_nb bpext perfctr_llc mwaitx cpb hw_pstate ssbd ibpb vmmcall fsgsbase bmi1 avx2 smep bmi2 rdseed adx smap clflushopt sha_ni xsaveopt xsavec xgetbv1 xsaves clzero irperf xsaveerptr arat npt lbrv svm_lock
                         nrip_save tsc_scale vmcb_clean flushbyasid decodeassists pausefilter pfthreshold avic v_vmsave_vmload vgif overflow_recov succor smca sme sev
Virtualization features: 
  Virtualization:        AMD-V
Caches (sum of all):     
  L1d:                   256 KiB (8 instances)
  L1i:                   512 KiB (8 instances)
  L2:                    4 MiB (8 instances)
  L3:                    16 MiB (2 instances)
NUMA:                    
  NUMA node(s):          1
  NUMA node0 CPU(s):     0-15
```
