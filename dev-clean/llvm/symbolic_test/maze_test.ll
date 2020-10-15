; ModuleID = 'maze_test.c'
source_filename = "maze_test.c"
target datalayout = "e-m:e-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-unknown-linux-gnu"

@maze = dso_local global [7 x [11 x i8]] [[11 x i8] c"+-+---+---+", [11 x i8] c"| |     |#|", [11 x i8] c"| | --+ | |", [11 x i8] c"| |   | | |", [11 x i8] c"| +-- | | |", [11 x i8] c"|     |   |", [11 x i8] c"+-----+---+"], align 16
@.str = private unnamed_addr constant [3 x i8] c"%c\00", align 1
@.str.1 = private unnamed_addr constant [2 x i8] c"\0A\00", align 1
@.str.2 = private unnamed_addr constant [24 x i8] c"Maze dimensions: %dx%d\0A\00", align 1
@.str.3 = private unnamed_addr constant [19 x i8] c"Player pos: %dx%d\0A\00", align 1
@.str.4 = private unnamed_addr constant [18 x i8] c"Iteration no. %d\0A\00", align 1
@.str.5 = private unnamed_addr constant [67 x i8] c"Program the player moves with a sequence of 'w', 's', 'a' and 'd'\0A\00", align 1
@.str.6 = private unnamed_addr constant [28 x i8] c"Try to reach the price(#)!\0A\00", align 1
@.str.7 = private unnamed_addr constant [40 x i8] c"Wrong command!(only w,s,a,d accepted!)\0A\00", align 1
@.str.8 = private unnamed_addr constant [12 x i8] c"You loose!\0A\00", align 1
@.str.9 = private unnamed_addr constant [10 x i8] c"You win!\0A\00", align 1
@.str.10 = private unnamed_addr constant [22 x i8] c"Your solution <%42s>\0A\00", align 1
@.str.11 = private unnamed_addr constant [34 x i8] c"Iteration no. %d. Action: %c. %s\0A\00", align 1
@.str.12 = private unnamed_addr constant [9 x i8] c"Blocked!\00", align 1
@.str.13 = private unnamed_addr constant [1 x i8] zeroinitializer, align 1
@.str.14 = private unnamed_addr constant [11 x i8] c"You loose\0A\00", align 1

; Function Attrs: noinline nounwind optnone uwtable
define dso_local void @draw() #0 {
entry:
  %i = alloca i32, align 4
  %j = alloca i32, align 4
  store i32 0, i32* %i, align 4
  br label %for.cond

for.cond:                                         ; preds = %for.inc7, %entry
  %0 = load i32, i32* %i, align 4
  %cmp = icmp slt i32 %0, 7
  br i1 %cmp, label %for.body, label %for.end9

for.body:                                         ; preds = %for.cond
  store i32 0, i32* %j, align 4
  br label %for.cond1

for.cond1:                                        ; preds = %for.inc, %for.body
  %1 = load i32, i32* %j, align 4
  %cmp2 = icmp slt i32 %1, 11
  br i1 %cmp2, label %for.body3, label %for.end

for.body3:                                        ; preds = %for.cond1
  %2 = load i32, i32* %i, align 4
  %idxprom = sext i32 %2 to i64
  %arrayidx = getelementptr inbounds [7 x [11 x i8]], [7 x [11 x i8]]* @maze, i64 0, i64 %idxprom
  %3 = load i32, i32* %j, align 4
  %idxprom4 = sext i32 %3 to i64
  %arrayidx5 = getelementptr inbounds [11 x i8], [11 x i8]* %arrayidx, i64 0, i64 %idxprom4
  %4 = load i8, i8* %arrayidx5, align 1
  %conv = sext i8 %4 to i32
  %call = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.str, i64 0, i64 0), i32 %conv)
  br label %for.inc

for.inc:                                          ; preds = %for.body3
  %5 = load i32, i32* %j, align 4
  %inc = add nsw i32 %5, 1
  store i32 %inc, i32* %j, align 4
  br label %for.cond1

for.end:                                          ; preds = %for.cond1
  %call6 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([2 x i8], [2 x i8]* @.str.1, i64 0, i64 0))
  br label %for.inc7

for.inc7:                                         ; preds = %for.end
  %6 = load i32, i32* %i, align 4
  %inc8 = add nsw i32 %6, 1
  store i32 %inc8, i32* %i, align 4
  br label %for.cond

for.end9:                                         ; preds = %for.cond
  %call10 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([2 x i8], [2 x i8]* @.str.1, i64 0, i64 0))
  ret void
}

declare dso_local i32 @printf(i8*, ...) #1

; Function Attrs: noinline nounwind optnone uwtable
define dso_local i32 @main(i32 %argc, i8** %argv) #0 {
entry:
  %retval = alloca i32, align 4
  %argc.addr = alloca i32, align 4
  %argv.addr = alloca i8**, align 8
  %x = alloca i32, align 4
  %y = alloca i32, align 4
  %ox = alloca i32, align 4
  %oy = alloca i32, align 4
  %i = alloca i32, align 4
  %program = alloca [28 x i8], align 16
  store i32 0, i32* %retval, align 4
  store i32 %argc, i32* %argc.addr, align 4
  store i8** %argv, i8*** %argv.addr, align 8
  store i32 0, i32* %i, align 4
  store i32 1, i32* %x, align 4
  store i32 1, i32* %y, align 4
  %0 = load i32, i32* %y, align 4
  %idxprom = sext i32 %0 to i64
  %arrayidx = getelementptr inbounds [7 x [11 x i8]], [7 x [11 x i8]]* @maze, i64 0, i64 %idxprom
  %1 = load i32, i32* %x, align 4
  %idxprom1 = sext i32 %1 to i64
  %arrayidx2 = getelementptr inbounds [11 x i8], [11 x i8]* %arrayidx, i64 0, i64 %idxprom1
  store i8 88, i8* %arrayidx2, align 1
  %call = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([24 x i8], [24 x i8]* @.str.2, i64 0, i64 0), i32 11, i32 7)
  %2 = load i32, i32* %x, align 4
  %3 = load i32, i32* %y, align 4
  %call3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([19 x i8], [19 x i8]* @.str.3, i64 0, i64 0), i32 %2, i32 %3)
  %4 = load i32, i32* %i, align 4
  %call4 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([18 x i8], [18 x i8]* @.str.4, i64 0, i64 0), i32 %4)
  %call5 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([67 x i8], [67 x i8]* @.str.5, i64 0, i64 0))
  %call6 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([28 x i8], [28 x i8]* @.str.6, i64 0, i64 0))
  call void @draw()
  %arraydecay = getelementptr inbounds [28 x i8], [28 x i8]* %program, i64 0, i64 0
  %call7 = call i32 (i8*, i32, ...) bitcast (i32 (...)* @make_symbolic to i32 (i8*, i32, ...)*)(i8* %arraydecay, i32 29)
  br label %while.cond

while.cond:                                       ; preds = %if.end69, %entry
  %5 = load i32, i32* %i, align 4
  %cmp = icmp slt i32 %5, 28
  br i1 %cmp, label %while.body, label %while.end

while.body:                                       ; preds = %while.cond
  %6 = load i32, i32* %x, align 4
  store i32 %6, i32* %ox, align 4
  %7 = load i32, i32* %y, align 4
  store i32 %7, i32* %oy, align 4
  %8 = load i32, i32* %i, align 4
  %idxprom8 = sext i32 %8 to i64
  %arrayidx9 = getelementptr inbounds [28 x i8], [28 x i8]* %program, i64 0, i64 %idxprom8
  %9 = load i8, i8* %arrayidx9, align 1
  %conv = sext i8 %9 to i32
  switch i32 %conv, label %sw.default [
    i32 119, label %sw.bb
    i32 115, label %sw.bb10
    i32 97, label %sw.bb11
    i32 100, label %sw.bb13
  ]

sw.bb:                                            ; preds = %while.body
  %10 = load i32, i32* %y, align 4
  %dec = add nsw i32 %10, -1
  store i32 %dec, i32* %y, align 4
  br label %sw.epilog

sw.bb10:                                          ; preds = %while.body
  %11 = load i32, i32* %y, align 4
  %inc = add nsw i32 %11, 1
  store i32 %inc, i32* %y, align 4
  br label %sw.epilog

sw.bb11:                                          ; preds = %while.body
  %12 = load i32, i32* %x, align 4
  %dec12 = add nsw i32 %12, -1
  store i32 %dec12, i32* %x, align 4
  br label %sw.epilog

sw.bb13:                                          ; preds = %while.body
  %13 = load i32, i32* %x, align 4
  %inc14 = add nsw i32 %13, 1
  store i32 %inc14, i32* %x, align 4
  br label %sw.epilog

sw.default:                                       ; preds = %while.body
  %call15 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([40 x i8], [40 x i8]* @.str.7, i64 0, i64 0))
  %call16 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([12 x i8], [12 x i8]* @.str.8, i64 0, i64 0))
  call void @exit(i32 -1) #3
  unreachable

sw.epilog:                                        ; preds = %sw.bb13, %sw.bb11, %sw.bb10, %sw.bb
  %14 = load i32, i32* %y, align 4
  %idxprom17 = sext i32 %14 to i64
  %arrayidx18 = getelementptr inbounds [7 x [11 x i8]], [7 x [11 x i8]]* @maze, i64 0, i64 %idxprom17
  %15 = load i32, i32* %x, align 4
  %idxprom19 = sext i32 %15 to i64
  %arrayidx20 = getelementptr inbounds [11 x i8], [11 x i8]* %arrayidx18, i64 0, i64 %idxprom19
  %16 = load i8, i8* %arrayidx20, align 1
  %conv21 = sext i8 %16 to i32
  %cmp22 = icmp eq i32 %conv21, 35
  br i1 %cmp22, label %if.then, label %if.end

if.then:                                          ; preds = %sw.epilog
  %call24 = call i32 (...) @assert()
  %call25 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([10 x i8], [10 x i8]* @.str.9, i64 0, i64 0))
  %arraydecay26 = getelementptr inbounds [28 x i8], [28 x i8]* %program, i64 0, i64 0
  %call27 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([22 x i8], [22 x i8]* @.str.10, i64 0, i64 0), i8* %arraydecay26)
  call void @exit(i32 1) #3
  unreachable

if.end:                                           ; preds = %sw.epilog
  %17 = load i32, i32* %y, align 4
  %idxprom28 = sext i32 %17 to i64
  %arrayidx29 = getelementptr inbounds [7 x [11 x i8]], [7 x [11 x i8]]* @maze, i64 0, i64 %idxprom28
  %18 = load i32, i32* %x, align 4
  %idxprom30 = sext i32 %18 to i64
  %arrayidx31 = getelementptr inbounds [11 x i8], [11 x i8]* %arrayidx29, i64 0, i64 %idxprom30
  %19 = load i8, i8* %arrayidx31, align 1
  %conv32 = sext i8 %19 to i32
  %cmp33 = icmp ne i32 %conv32, 32
  br i1 %cmp33, label %land.lhs.true, label %if.end52

land.lhs.true:                                    ; preds = %if.end
  %20 = load i32, i32* %y, align 4
  %cmp35 = icmp eq i32 %20, 2
  br i1 %cmp35, label %land.lhs.true37, label %if.then51

land.lhs.true37:                                  ; preds = %land.lhs.true
  %21 = load i32, i32* %y, align 4
  %idxprom38 = sext i32 %21 to i64
  %arrayidx39 = getelementptr inbounds [7 x [11 x i8]], [7 x [11 x i8]]* @maze, i64 0, i64 %idxprom38
  %22 = load i32, i32* %x, align 4
  %idxprom40 = sext i32 %22 to i64
  %arrayidx41 = getelementptr inbounds [11 x i8], [11 x i8]* %arrayidx39, i64 0, i64 %idxprom40
  %23 = load i8, i8* %arrayidx41, align 1
  %conv42 = sext i8 %23 to i32
  %cmp43 = icmp eq i32 %conv42, 124
  br i1 %cmp43, label %land.lhs.true45, label %if.then51

land.lhs.true45:                                  ; preds = %land.lhs.true37
  %24 = load i32, i32* %x, align 4
  %cmp46 = icmp sgt i32 %24, 0
  br i1 %cmp46, label %land.lhs.true48, label %if.then51

land.lhs.true48:                                  ; preds = %land.lhs.true45
  %25 = load i32, i32* %x, align 4
  %cmp49 = icmp slt i32 %25, 11
  br i1 %cmp49, label %if.end52, label %if.then51

if.then51:                                        ; preds = %land.lhs.true48, %land.lhs.true45, %land.lhs.true37, %land.lhs.true
  %26 = load i32, i32* %ox, align 4
  store i32 %26, i32* %x, align 4
  %27 = load i32, i32* %oy, align 4
  store i32 %27, i32* %y, align 4
  br label %if.end52

if.end52:                                         ; preds = %if.then51, %land.lhs.true48, %if.end
  %28 = load i32, i32* %x, align 4
  %29 = load i32, i32* %y, align 4
  %call53 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([19 x i8], [19 x i8]* @.str.3, i64 0, i64 0), i32 %28, i32 %29)
  %30 = load i32, i32* %i, align 4
  %31 = load i32, i32* %i, align 4
  %idxprom54 = sext i32 %31 to i64
  %arrayidx55 = getelementptr inbounds [28 x i8], [28 x i8]* %program, i64 0, i64 %idxprom54
  %32 = load i8, i8* %arrayidx55, align 1
  %conv56 = sext i8 %32 to i32
  %33 = load i32, i32* %ox, align 4
  %34 = load i32, i32* %x, align 4
  %cmp57 = icmp eq i32 %33, %34
  br i1 %cmp57, label %land.rhs, label %land.end

land.rhs:                                         ; preds = %if.end52
  %35 = load i32, i32* %oy, align 4
  %36 = load i32, i32* %y, align 4
  %cmp59 = icmp eq i32 %35, %36
  br label %land.end

land.end:                                         ; preds = %land.rhs, %if.end52
  %37 = phi i1 [ false, %if.end52 ], [ %cmp59, %land.rhs ]
  %38 = zext i1 %37 to i64
  %cond = select i1 %37, i8* getelementptr inbounds ([9 x i8], [9 x i8]* @.str.12, i64 0, i64 0), i8* getelementptr inbounds ([1 x i8], [1 x i8]* @.str.13, i64 0, i64 0)
  %call61 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([34 x i8], [34 x i8]* @.str.11, i64 0, i64 0), i32 %30, i32 %conv56, i8* %cond)
  %39 = load i32, i32* %ox, align 4
  %40 = load i32, i32* %x, align 4
  %cmp62 = icmp eq i32 %39, %40
  br i1 %cmp62, label %land.lhs.true64, label %if.end69

land.lhs.true64:                                  ; preds = %land.end
  %41 = load i32, i32* %oy, align 4
  %42 = load i32, i32* %y, align 4
  %cmp65 = icmp eq i32 %41, %42
  br i1 %cmp65, label %if.then67, label %if.end69

if.then67:                                        ; preds = %land.lhs.true64
  %call68 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([11 x i8], [11 x i8]* @.str.14, i64 0, i64 0))
  call void @exit(i32 -2) #3
  unreachable

if.end69:                                         ; preds = %land.lhs.true64, %land.end
  %43 = load i32, i32* %y, align 4
  %idxprom70 = sext i32 %43 to i64
  %arrayidx71 = getelementptr inbounds [7 x [11 x i8]], [7 x [11 x i8]]* @maze, i64 0, i64 %idxprom70
  %44 = load i32, i32* %x, align 4
  %idxprom72 = sext i32 %44 to i64
  %arrayidx73 = getelementptr inbounds [11 x i8], [11 x i8]* %arrayidx71, i64 0, i64 %idxprom72
  store i8 88, i8* %arrayidx73, align 1
  call void @draw()
  %45 = load i32, i32* %i, align 4
  %inc74 = add nsw i32 %45, 1
  store i32 %inc74, i32* %i, align 4
  %call75 = call i32 (i32, ...) bitcast (i32 (...)* @sleep to i32 (i32, ...)*)(i32 1)
  br label %while.cond

while.end:                                        ; preds = %while.cond
  %call76 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([11 x i8], [11 x i8]* @.str.14, i64 0, i64 0))
  %46 = load i32, i32* %retval, align 4
  ret i32 %46
}

declare dso_local i32 @make_symbolic(...) #1

; Function Attrs: noreturn nounwind
declare dso_local void @exit(i32) #2

declare dso_local i32 @assert(...) #1

declare dso_local i32 @sleep(...) #1

attributes #0 = { noinline nounwind optnone uwtable "correctly-rounded-divide-sqrt-fp-math"="false" "disable-tail-calls"="false" "less-precise-fpmad"="false" "min-legal-vector-width"="0" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-jump-tables"="false" "no-nans-fp-math"="false" "no-signed-zeros-fp-math"="false" "no-trapping-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+cx8,+fxsr,+mmx,+sse,+sse2,+x87" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #1 = { "correctly-rounded-divide-sqrt-fp-math"="false" "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "no-signed-zeros-fp-math"="false" "no-trapping-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+cx8,+fxsr,+mmx,+sse,+sse2,+x87" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #2 = { noreturn nounwind "correctly-rounded-divide-sqrt-fp-math"="false" "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "no-signed-zeros-fp-math"="false" "no-trapping-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+cx8,+fxsr,+mmx,+sse,+sse2,+x87" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #3 = { noreturn nounwind }

!llvm.module.flags = !{!0}
!llvm.ident = !{!1}

!0 = !{i32 1, !"wchar_size", i32 4}
!1 = !{!"clang version 9.0.0 (tags/RELEASE_900/final)"}
