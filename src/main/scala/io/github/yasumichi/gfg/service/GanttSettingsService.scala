package io.github.yasumichi.gfg.service

import java.io.File
import scala.util.Using
import gitbucket.core.util.Directory._
import gitbucket.core.util.SyntaxSugars._
import GanttSettingsService._

trait GanttSettingsService {

  val ganttConf = new File(GitBucketHome, "flexible-gantt.conf")

  def  saveGanttSettings(settings: GanttSettings): Unit = {
    val props = new java.util.Properties()
    props.setProperty(ganttLocale, settings.ganttLocale)
    Using.resource(new java.io.FileOutputStream(ganttConf)) { out =>
      props.store(out, null)
    }
  }

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

object GanttSettingsService {
  import scala.reflect.ClassTag    

  case class GanttSettings(ganttLocale: String)

  private val ganttLocale = "ganttLocale"

  private def getValue[A: ClassTag](props: java.util.Properties,
                                    key: String,
                                    default: A): A = {
    val value = props.getProperty(key)
    if (value == null || value.isEmpty) default
    else convertType(value).asInstanceOf[A]
  }

  private def getOptionValue[A: ClassTag](props: java.util.Properties,
                                          key: String,
                                          default: Option[A]): Option[A] = {
    val value = props.getProperty(key)
    if (value == null || value.isEmpty) default
    else Some(convertType(value)).asInstanceOf[Option[A]]
  }

  private def convertType[A: ClassTag](value: String) = {
    val c = implicitly[ClassTag[A]].runtimeClass
    if (c == classOf[Boolean]) value.toBoolean
    else if (c == classOf[Int]) value.toInt
    else value
  }
}
