package io.github.yasumichi.gfg.service

import GanttSettingsService._
import gitbucket.core.util.Directory._
import gitbucket.core.util.SyntaxSugars._
import java.io.File
import scala.util.Using

/**
  * trait for system settings of Flexible Gantt
  */
trait GanttSettingsService {

  // configuration file of Flexible Gantt
  val ganttConf = new File(GitBucketHome, "flexible-gantt.conf")

  /**
    * save settings of Flexible Gantt
    *
    * @param settings settings of Flexible Gantt
    */
  def  saveGanttSettings(settings: GanttSettings): Unit = {
    val props = new java.util.Properties()
    props.setProperty(ganttLocale, settings.ganttLocale)
    Using.resource(new java.io.FileOutputStream(ganttConf)) { out =>
      props.store(out, null)
    }
  }

  /**
    * load settings of Flexible Gantt
    *
    * @return settings of Flexible Gantt
    */
  def loadGanttSettings(): GanttSettings = {
    val props = new java.util.Properties()
    if (ganttConf.exists) {
      Using.resource(new java.io.FileInputStream(ganttConf)) { in =>
        props.load(in)
      }
    }
    GanttSettings(
      getValue[String](props, ganttLocale, "en")  
    )
  }
}

/**
  * companion object for GanttSettingsSerivice
  */
object GanttSettingsService {
  import scala.reflect.ClassTag    

  // case class for settings of Flexible Gantt
  case class GanttSettings(ganttLocale: String)

  private val ganttLocale = "ganttLocale"

  /**
    * get value of settings of Flexible Gantt
    *
    * @param props properties
    * @param key name of setting
    * @param default default value
    * @return value of settings
    */
  private def getValue[A: ClassTag](props: java.util.Properties,
                                    key: String,
                                    default: A): A = {
    val value = props.getProperty(key)
    if (value == null || value.isEmpty) default
    else convertType(value).asInstanceOf[A]
  }

  /**
    *  get value of settings of Flexible Gantt (Option type)
    *
    * @param props properties
    * @param key name of setting
    * @param default default value
    * @return value of settings
    */
  private def getOptionValue[A: ClassTag](props: java.util.Properties,
                                          key: String,
                                          default: Option[A]): Option[A] = {
    val value = props.getProperty(key)
    if (value == null || value.isEmpty) default
    else Some(convertType(value)).asInstanceOf[Option[A]]
  }

  /**
    * convert value type
    *
    * @param value string of value
    * @return converted value
    */
  private def convertType[A: ClassTag](value: String) = {
    val c = implicitly[ClassTag[A]].runtimeClass
    if (c == classOf[Boolean]) value.toBoolean
    else if (c == classOf[Int]) value.toInt
    else value
  }
}
