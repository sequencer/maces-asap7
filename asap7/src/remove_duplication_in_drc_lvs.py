#!/usr/bin/python
import sys
import re
import shutil

"""
Remove conflicting specification statements found in PDK's DRC & LVS decks.
"""
# asap7PDK_r1p5.tar.bz2/asap7PDK_r1p5/calibre/ruledirs/drc/drcRules_calibre_asap7_171111a.rul
# asap7PDK_r1p5.tar.bz2/asap7PDK_r1p5/calibre/ruledirs/lvs/lvsRules_calibre_asap7_160819a.rul
input_deck = sys.argv[1]
output_deck = sys.argv[2]
pattern = re.compile(".*(LAYOUT PATH|LAYOUT PRIMARY|LAYOUT SYSTEM|DRC RESULTS DATABASE|DRC SUMMARY REPORT|LVS REPORT|LVS POWER NAME|LVS GROUND NAME).*\n")
with open(output_deck, 'w') as out_f:
    with open(input_deck, 'r') as in_f:
        out_f.write(pattern.sub("", in_f.read()))
    shutil.copystat(input_deck, out_f.name)
    shutil.copy(out_f.name, input_deck)
