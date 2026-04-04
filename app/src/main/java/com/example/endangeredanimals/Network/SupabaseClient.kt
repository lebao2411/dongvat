package com.example.endangeredanimals.Network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseInstance {
    val client = createSupabaseClient(
        supabaseUrl = "https://ehtlxhoymxclqevouozp.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVodGx4aG95bXhjbHFldm91b3pwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzE2NTg1MjQsImV4cCI6MjA4NzIzNDUyNH0.Dk-iN-SfT2RPk-dkMDXpwMz6w7-ggaexhG954UsHX0g"
    ) {
        install(Postgrest)
        install(Auth)
    }
}
