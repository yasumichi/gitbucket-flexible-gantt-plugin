package io.github.yasumichi.gfg.model

import com.github.takezoe.slick.blocking.BlockingJdbcProfile
import gitbucket.core.model.CoreProfile

object Profile extends CoreProfile with IssuePeriodComponent
