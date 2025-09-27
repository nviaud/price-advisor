package com.nviaud.pricing.repositories

import com.nviaud.pricing.entities.Category
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long>