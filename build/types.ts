// class LauncherKt$sampleServer$1$2$6$JsonRequest
export interface LauncherKtsampleServer126JsonRequest {hello: string; required: string}
// class MyData
export interface MyData {hello: string; world: number}
// class users.Email
export type Email = string
// class users.Id
export type Id<T> = string
// class users.User$Address
export interface UserAddress {city: string; countryCode: string; id: Id<UserAddress>; userId: Id<User>}
// class users.User
export interface User {email: Email; firstName: string; id: Id<User>; lastName: string; locale: string; passwordHash: string}
