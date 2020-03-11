import $ivy.`me.sequencer::maces:0.0.1`
import maces._
import Utils._

case class LibTuple(libType: String, nominalType: String, vt: String)

object LibTuple {
  implicit def rw: upickle.default.ReadWriter[LibTuple] = upickle.default.macroRW
}

object asap7 extends MacesModule {
  def wholetar = T.sources {
    unpack(getResourceFile("ASAP7_PDKandLIB.tar")().path)
  }

  def stdlib = T {
    unpack(getFile(getFiles(wholetar()), "asap7libs_24.tar.bz2").path).pathRef
  }

  def pdk = T {
    unpack(getFile(getFiles(wholetar()), "asap7PDK_r1p5.tar.bz2").path).pathRef
  }

  def librariesFiles = T {
    val cwd = T.ctx.dest
    val rawFiles = getFiles(Seq(stdlib(), pdk()))
    val sramCdlFile = getFile(rawFiles, "asap7_75t_SRAM.cdl")
    val patchedCdlFile = newFile("asap7_75t_SRAM.cdl", cwd).pathRef
    pythonPatch(
      getSourceFile("fix_sram_cdl_bug.py")().path,
      Seq(sramCdlFile.path.toString, patchedCdlFile.path.toString)
    )
    val originalGds = getFile(rawFiles, "asap7sc7p5t_24.gds")
    val newGds: Seq[PathRef] = pythonPatch(
      getSourceFile("generate_multi_vt_gds.py")().path,
      Seq(originalGds.path.toString)
    ).out.lines.map(os.Path(_).pathRef)
    val originalLvsDeck = getFile(rawFiles, "lvsRules_calibre_asap7_160819a.rul")
    val patchedLvsDeck = newFile("lvsRules_calibre_asap7_160819a.rul", cwd).pathRef
    pythonPatch(
      getSourceFile("remove_duplication_in_drc_lvs.py")().path,
      Seq(originalLvsDeck.path.toString, patchedLvsDeck.path.toString)
    )
    val originalDrcDeck = getFile(rawFiles, "drcRules_calibre_asap7_171111a.rul")
    val patchedDrcDeck = newFile("drcRules_calibre_asap7_171111a.rul", cwd).pathRef
    pythonPatch(
      getSourceFile("remove_duplication_in_drc_lvs.py")().path,
      Seq(originalDrcDeck.path.toString, patchedDrcDeck.path.toString)
    )
    (rawFiles ++ newGds :+ patchedCdlFile :+ patchedDrcDeck :+ patchedLvsDeck) diff Seq(sramCdlFile, originalGds, originalDrcDeck, originalLvsDeck)
  }

  def pdkFiles = T {
    // TODO: need patches
    getFiles(pdk())
  }

  def getStdlibFile(name: String) = T.task {
    getFile(librariesFiles(), name)
  }

  def getPdkFile(name: String) = T.task {
    getFile(pdkFiles(), name)
  }

  def libraries = T.persistent {
    val libTypeSet = Set("ao", "invbuf", "oa", "seq", "simple")
    val nominalTypeSet = Set("ss", "tt", "ff")
    val vtSet = Set("rvt", "lvt", "slvt", "sram")
    (libTypeSet cross nominalTypeSet cross vtSet).toSeq.map {
      case (libType: String, nominalType: String, vt: String) =>
        val name = s"${libType}_${vt}_${nominalType}"
        val voltage = nominalType match {
          case "ss" => 0.63
          case "tt" => 0.70
          case "ff" => 0.77
        }
        val temperature = nominalType match {
          case "ss" => 100
          case "tt" => 25
          case "ff" => 0
        }
        val vtShortName = vt.replace("vt", "").toUpperCase
        Library(
          name, voltage, temperature, nominalType,
          voltageThreshold = Some(vt),
          libertyName = Some(s"asap7sc7p5t_24_${libType.toUpperCase}_${vt.toUpperCase}_${nominalType.toUpperCase()}.lib"),
          qrcTechName = Some("qrcTechFile_typ03_scaled4xV06"),
          lefName = Some(s"asap7sc7p5t_24_${vtShortName}_4x_170912.lef"),
          spiceName = Some(s"asap7_75t_${vtShortName}.cdl"),
          gdsName = Some(s"asap7sc7p5t_24_${vtShortName}.gds"),
          fileList = librariesFiles().map(_.path)
        )
    }
  }

  def lvsDeck = T {
    getPdkFile("lvsRules_calibre_asap7.rul")
  }

  def drcDeck = T {
    getPdkFile("drcRules_calibre_asap7.rul")
  }

  def layerMap = T {
    getPdkFile("asap7_fromAPR.layermap")
  }

  def techLef = T {
    getStdlibFile("asap7_tech_4x_170803.lef")
  }

  // additional metadata below
  def physicalOnlyCells = T {
    Seq(
      "TAPCELL_ASAP7_75t_R", "TAPCELL_ASAP7_75t_L", "TAPCELL_ASAP7_75t_SL", "TAPCELL_ASAP7_75t_SRAM",
      "TAPCELL_WITH_FILLER_ASAP7_75t_R", "TAPCELL_WITH_FILLER_ASAP7_75t_L", "TAPCELL_WITH_FILLER_ASAP7_75t_SL", "TAPCELL_WITH_FILLER_ASAP7_75t_SRAM",
      "FILLER_ASAP7_75t_R", "FILLER_ASAP7_75t_L", "FILLER_ASAP7_75t_SL", "FILLER_ASAP7_75t_SRAM",
      "FILLERxp5_ASAP7_75t_R", "FILLERxp5_ASAP7_75t_L", "FILLERxp5_ASAP7_75t_SL", "FILLERxp5_ASAP7_75t_SRAM",
      "DECAPx1_ASAP7_75t_R", "DECAPx1_ASAP7_75t_L", "DECAPx1_ASAP7_75t_SL", "DECAPx1_ASAP7_75t_SRAM",
      "DECAPx2_ASAP7_75t_R", "DECAPx2_ASAP7_75t_L", "DECAPx2_ASAP7_75t_SL", "DECAPx2_ASAP7_75t_SRAM",
      "DECAPx4_ASAP7_75t_R", "DECAPx4_ASAP7_75t_L", "DECAPx4_ASAP7_75t_SL", "DECAPx4_ASAP7_75t_SRAM",
      "DECAPx6_ASAP7_75t_R", "DECAPx6_ASAP7_75t_L", "DECAPx6_ASAP7_75t_SL", "DECAPx6_ASAP7_75t_SRAM",
      "DECAPx10_ASAP7_75t_R", "DECAPx10_ASAP7_75t_L", "DECAPx10_ASAP7_75t_SL", "DECAPx10_ASAP7_75t_SRAM"
    )
  }

  def tapCells = T {
    Seq(
      "TAPCELL_ASAP7_75t_R", "TAPCELL_ASAP7_75t_SL", "TAPCELL_ASAP7_75t_L", "TAPCELL_ASAP7_75t_SRAM"
    )
  }

  def fillerCells = T {
    Seq(
      "FILLER_ASAP7_75t_R", "FILLER_ASAP7_75t_L", "FILLER_ASAP7_75t_SL", "FILLER_ASAP7_75t_SRAM",
      "FILLERxp5_ASAP7_75t_R", "FILLERxp5_ASAP7_75t_L", "FILLERxp5_ASAP7_75t_SL", "FILLERxp5_ASAP7_75t_SRAM",
      "DECAPx1_ASAP7_75t_R", "DECAPx1_ASAP7_75t_L", "DECAPx1_ASAP7_75t_SL", "DECAPx1_ASAP7_75t_SRAM",
      "DECAPx2_ASAP7_75t_R", "DECAPx2_ASAP7_75t_L", "DECAPx2_ASAP7_75t_SL", "DECAPx2_ASAP7_75t_SRAM",
      "DECAPx4_ASAP7_75t_R", "DECAPx4_ASAP7_75t_L", "DECAPx4_ASAP7_75t_SL", "DECAPx4_ASAP7_75t_SRAM",
      "DECAPx6_ASAP7_75t_R", "DECAPx6_ASAP7_75t_L", "DECAPx6_ASAP7_75t_SL", "DECAPx6_ASAP7_75t_SRAM",
      "DECAPx10_ASAP7_75t_R", "DECAPx10_ASAP7_75t_L", "DECAPx10_ASAP7_75t_SL", "DECAPx10_ASAP7_75t_SRAM"
    )
  }

  def tieHighCells = T {
    Seq(
      "TIEHIx1_ASAP7_75t_R", "TIEHIx1_ASAP7_75t_L", "TIEHIx1_ASAP7_75t_SL", "TIEHIx1_ASAP7_75t_SRAM"
    )
  }

  def tieLowCells = T {
    Seq(
      "TIEHIx1_ASAP7_75t_R", "TIEHIx1_ASAP7_75t_L", "TIEHIx1_ASAP7_75t_SL", "TIEHIx1_ASAP7_75t_SRAM"
    )
  }

  def gridUnit = T {
    0.001
  }

  def timeUnit = T {
    "ps"
  }
}
