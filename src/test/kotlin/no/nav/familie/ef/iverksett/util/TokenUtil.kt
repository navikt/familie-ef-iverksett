package no.nav.familie.ef.iverksett.util

import com.nimbusds.jwt.JWTClaimsSet
import no.nav.security.token.support.test.JwkGenerator
import no.nav.security.token.support.test.JwtTokenGenerator
import java.util.*

fun onBehalfOfToken(role: String? = "rolle", saksbehandler: String = "julenissen"): String {
    val thisId = UUID.randomUUID().toString()
    val clientId = UUID.randomUUID().toString()
    var claimsSet = JwtTokenGenerator.createSignedJWT(thisId).jwtClaimsSet // default claimSet
    val builder = JWTClaimsSet.Builder(claimsSet)
        .claim("oid", saksbehandler)
        .claim("sub", thisId)
        .claim("azp", clientId)
        .claim("NAVident", saksbehandler)
        .claim("groups", listOf(role))

    claimsSet = builder.build()
    return JwtTokenGenerator.createSignedJWT(JwkGenerator.getDefaultRSAKey(), claimsSet).serialize()
}