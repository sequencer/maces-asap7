import sys
import re
"""
vendor's SRAM cdl use slvt cell, this patch will sed cells name in which, fix this bug.
Usage:

python fix_sram_cdl_bug.py input output
"""
pattern0 = re.compile("SL")
pattern1 = re.compile("slvt")
# output
with open(sys.argv[2], 'w') as out_f:
    # input asap7libs_24.tar.bz2/asap7libs_24/cdl/lvs/asap7_75t_SRAM.cdl
    with open(sys.argv[1], 'r') as in_f:
        out_f.write(pattern1.sub("sram", pattern0.sub("SRAM", in_f.read())).encode('utf-8'))
