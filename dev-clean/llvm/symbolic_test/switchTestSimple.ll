; ModuleID = 'switchTestSimple.c'
source_filename = "switchTestSimple.c"
target datalayout = "e-m:e-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-unknown-linux-gnu"

; Function Attrs: noinline nounwind optnone uwtable
define dso_local i32 @main() #0 {
entry:
  %retval = alloca i32, align 4
  %a = alloca i32, align 4
  store i32 0, i32* %retval, align 4
  %call = call i32 (i32*, i32, ...) bitcast (i32 (...)* @make_symbolic to i32 (i32*, i32, ...)*)(i32* %a, i32 1)
  %0 = load i32, i32* %a, align 4
  switch i32 %0, label %sw.default [
    i32 1, label %sw.bb
    i32 2, label %sw.bb2
    i32 3, label %sw.bb4
    i32 4, label %sw.bb6
  ]

sw.bb:                                            ; preds = %entry
  %call1 = call i32 (...) @assert()
  br label %sw.epilog

sw.bb2:                                           ; preds = %entry
  %call3 = call i32 (...) @assert()
  br label %sw.epilog

sw.bb4:                                           ; preds = %entry
  %call5 = call i32 (...) @assert()
  br label %sw.epilog

sw.bb6:                                           ; preds = %entry
  %call7 = call i32 (...) @assert()
  br label %sw.epilog

sw.default:                                       ; preds = %entry
  %call8 = call i32 (...) @assert()
  br label %sw.epilog

sw.epilog:                                        ; preds = %sw.default, %sw.bb6, %sw.bb4, %sw.bb2, %sw.bb
  %1 = load i32, i32* %retval, align 4
  ret i32 %1
}

declare dso_local i32 @make_symbolic(...) #1

declare dso_local i32 @assert(...) #1

attributes #0 = { noinline nounwind optnone uwtable "correctly-rounded-divide-sqrt-fp-math"="false" "disable-tail-calls"="false" "less-precise-fpmad"="false" "min-legal-vector-width"="0" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-jump-tables"="false" "no-nans-fp-math"="false" "no-signed-zeros-fp-math"="false" "no-trapping-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+cx8,+fxsr,+mmx,+sse,+sse2,+x87" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #1 = { "correctly-rounded-divide-sqrt-fp-math"="false" "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "no-signed-zeros-fp-math"="false" "no-trapping-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+cx8,+fxsr,+mmx,+sse,+sse2,+x87" "unsafe-fp-math"="false" "use-soft-float"="false" }

!llvm.module.flags = !{!0}
!llvm.ident = !{!1}

!0 = !{i32 1, !"wchar_size", i32 4}
!1 = !{!"clang version 9.0.0 (tags/RELEASE_900/final)"}