package com.kayakpro.erg.interfaces

import com.kayakpro.erg.model.RBody

interface OnClickDelete {
        fun clickDelete(model: RBody)

        fun clickSava(model: RBody)
        fun clickEdit(model: RBody)
    }