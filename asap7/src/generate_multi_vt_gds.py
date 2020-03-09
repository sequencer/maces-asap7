import sys
import os
import gdspy

"""
PDK GDS only contains SLVT cells.
This patch will generate the other 3(LVT, RVT, SRAM) VT GDS files.
Usage:
python generate_multi_vt_gds.py target_dir
will print 4 gds path in stdout
"""

# asap7libs_24.tar.bz2/asap7libs_24/gds/asap7sc7p5t_24.gds
orig_gds = sys.argv[1]
# load original_gds
asap7_original_gds = gdspy.GdsLibrary().read_gds(infile=orig_gds, units='import')
original_cells = asap7_original_gds.cells
# This is an extra cell in the original GDS that has no geometry inside
del original_cells['m1_template']
# required libs
multi_libs = {
    "R": {
        "lib": gdspy.GdsLibrary(),
        "mvt_layer": None,
        },
    "L": {
        "lib": gdspy.GdsLibrary(),
        "mvt_layer": 98
        },
    "SL": {
        "lib": gdspy.GdsLibrary(),
        "mvt_layer": 97
        },
    "SRAM": {
        "lib": gdspy.GdsLibrary(),
        "mvt_layer": 110
        },
}
# create new libs
for vt, multi_lib in multi_libs.items():
    multi_lib['lib'].name = asap7_original_gds.name.replace('SL', vt)

for cell in original_cells.values():
    poly_dict = cell.get_polygons(by_spec=True)
    # extract polygon from layer 100(the boundary for cell)
    boundary_polygon = poly_dict[(100, 0)]
    for vt, multi_lib in multi_libs.items():
        mvt_layer = multi_lib['mvt_layer']
        if mvt_layer:
            # copy boundary_polygon to mvt_layer to mark the this cell is a mvt cell.
            mvt_polygon = gdspy.PolygonSet(boundary_polygon, multi_lib['mvt_layer'], 0)
            mvt_cell = cell.copy(name=cell.name.replace('SL', vt), exclude_from_current=True, deep_copy=True).add(mvt_polygon)
        else:
            # RVT, just copy the cell
            mvt_cell = cell.copy(name=cell.name.replace('SL', vt), exclude_from_current=True, deep_copy=True)
        # add mvt_cell to corresponding multi_lib
        multi_lib['lib'].add(mvt_cell)

for vt, multi_lib in multi_libs.items():
    # write multi_lib
    filename = f"{sys.argv[2]}/{os.path.splitext(os.path.basename(orig_gds))[0]}_{vt}'.gds'"
    multi_lib['lib'].write_gds(filename)
    print(filename)
