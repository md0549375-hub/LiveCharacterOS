#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
OUT="${TMPDIR:-/tmp}/livecharacteros-engine-test.jar"
TMP="${TMPDIR:-/tmp}/livecharacteros-engine-test.kt"
cat > "$TMP" <<'KOT'
import com.killer.livecharacter.engine.CharacterEngine
import com.killer.livecharacter.model.CharacterProfile
import kotlin.math.hypot
fun check(ok: Boolean, message: String) { require(ok) { message } }
fun main() {
    val e = CharacterEngine(CharacterProfile("verify", "Verify"))
    e.update(1080f, 1920f, 0.016f)
    check(e.shoulderX.isFinite() && e.shoulderY.isFinite(), "non-finite shoulder")
    e.setTarget(1080f, 0f)
    repeat(120) { e.update(1080f, 1920f, 0.016f) }
    check(hypot(e.handX - e.shoulderX, e.handY - e.shoulderY) <= e.profile.armReachPx + 2f, "IK reach violation")
    val before = e.target
    e.setTarget(Float.NaN, 1f)
    check(e.target === before, "invalid target was accepted")
    e.clearTarget()
    check(e.target == null, "target did not clear")
    println("LIVE CHARACTER OS ENGINE VERIFICATION: PASS")
}
KOT
kotlinc "$ROOT/app/src/main/java/com/killer/livecharacter/model/Models.kt" "$ROOT/app/src/main/java/com/killer/livecharacter/engine/CharacterEngine.kt" "$TMP" -include-runtime -d "$OUT"
java -jar "$OUT"